package zero.conflict.archiview.post.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.post.infrastructure.persistence.PostPlaceCountBulkUpdater;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "post-place.count-write-back.enabled", havingValue = "true")
public class PostPlaceCountWriteBackScheduler {

    private final PostPlaceCountRedisRepository redisRepository;
    private final PostPlaceCountBulkUpdater bulkUpdater;
    @Value("${post-place.count-write-back.batch-size:200}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${post-place.count-write-back.fixed-delay-ms:3000}")
    public void flush() {
        List<PostPlaceCountDelta> deltas = redisRepository.popDeltas(Math.max(1, batchSize));
        if (deltas.isEmpty()) {
            return;
        }

        long startedAt = System.nanoTime();
        try {
            int updatedRows = bulkUpdater.applyDeltas(deltas);
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
            log.debug("Flushed postPlace count deltas in bulk. batchSize={}, updatedRows={}, durationMs={}",
                    deltas.size(), updatedRows, durationMs);
        } catch (Exception e) {
            for (PostPlaceCountDelta delta : deltas) {
                redisRepository.requeue(delta);
            }
            log.warn("Failed to flush postPlace count deltas in bulk, requeued all. batchSize={}", deltas.size(), e);
        }
    }
}
