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

    @Scheduled(fixedDelayString = "${outbox.post.relay.fixed-delay-ms:5000}")
    @Transactional
    public void relay() {
        if (!relayEnabled) {
            return;
        }

        List<PostOutboxEvent> events = postOutboxEventRepository.findPendingBatch(batchSize);
        if (events.isEmpty()) {
            return;
        }

        for (PostOutboxEvent event : events) {
            try {
                postOutboxEventPublisher.publish(event);
                event.markPublished(LocalDateTime.now());
            } catch (Exception ex) {
                log.error(
                        "Failed to publish post outbox event. outboxEventId={}, aggregateId={}, eventType={}",
                        event.getId(),
                        event.getAggregateId(),
                        event.getEventType(),
                        ex);
                event.markFailed(ex.getMessage());
            }
        }

        postOutboxEventRepository.saveAll(events);
    }
}
