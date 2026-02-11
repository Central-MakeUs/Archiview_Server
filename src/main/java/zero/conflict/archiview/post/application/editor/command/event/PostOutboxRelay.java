package zero.conflict.archiview.post.application.editor.command.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.port.out.PostOutboxEventRepository;
import zero.conflict.archiview.post.application.port.out.PostOutboxEventPublisher;
import zero.conflict.archiview.post.domain.PostOutboxEvent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostOutboxRelay {

    private final PostOutboxEventRepository postOutboxEventRepository;
    private final PostOutboxEventPublisher postOutboxEventPublisher;

    @Value("${outbox.post.relay.batch-size:100}")
    private int batchSize;

    @Value("${outbox.post.relay.enabled:true}")
    private boolean relayEnabled;

    @Value("${outbox.post.relay.max-retry-count:5}")
    private int maxRetryCount;

    @Value("${outbox.post.relay.backoff.initial-ms:5000}")
    private long initialBackoffMs;

    @Value("${outbox.post.relay.backoff.max-ms:300000}")
    private long maxBackoffMs;

    @Value("${outbox.post.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${outbox.post.cleanup.retention-days:7}")
    private long cleanupRetentionDays;

    @Value("${outbox.post.cleanup.batch-size:500}")
    private int cleanupBatchSize;

    @Scheduled(fixedDelayString = "${outbox.post.relay.fixed-delay-ms:5000}")
    @Transactional
    public void relay() {
        if (!relayEnabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<PostOutboxEvent> events = postOutboxEventRepository.findRelayBatch(now, batchSize);
        if (events.isEmpty()) {
            return;
        }

        for (PostOutboxEvent event : events) {
            try {
                postOutboxEventPublisher.publish(event);
                event.markPublished(now);
            } catch (Exception ex) {
                log.error(
                        "Failed to publish post outbox event. outboxEventId={}, aggregateId={}, eventType={}",
                        event.getId(),
                        event.getAggregateId(),
                        event.getEventType(),
                        ex);
                int nextRetryCount = event.getRetryCount() + 1;
                if (nextRetryCount >= maxRetryCount) {
                    event.markGiveUp(ex.getMessage());
                    continue;
                }
                event.markRetryFailed(ex.getMessage(), calculateNextRetryAt(now, nextRetryCount));
            }
        }

        postOutboxEventRepository.saveAll(events);
    }

    @Scheduled(fixedDelayString = "${outbox.post.cleanup.fixed-delay-ms:3600000}")
    @Transactional
    public void cleanupPublished() {
        if (!cleanupEnabled) {
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minus(cleanupRetentionDays, ChronoUnit.DAYS);
        List<PostOutboxEvent> targets = postOutboxEventRepository.findPublishedBatchBefore(cutoff, cleanupBatchSize);
        if (targets.isEmpty()) {
            return;
        }

        postOutboxEventRepository.deleteAll(targets);
        log.info("Cleaned up published outbox events. count={}, cutoff={}", targets.size(), cutoff);
    }

    private LocalDateTime calculateNextRetryAt(LocalDateTime baseTime, int retryCount) {
        long multiplier = 1L << Math.max(0, retryCount - 1);
        long computed = initialBackoffMs * multiplier;
        long delayMs = Math.min(computed, maxBackoffMs);
        return baseTime.plus(delayMs, ChronoUnit.MILLIS);
    }
}
