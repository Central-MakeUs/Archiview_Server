package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostOutboxEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface PostOutboxEventRepository {

    PostOutboxEvent save(PostOutboxEvent event);

    List<PostOutboxEvent> saveAll(List<PostOutboxEvent> events);

    List<PostOutboxEvent> findRelayBatch(LocalDateTime now, int batchSize);

    List<PostOutboxEvent> findPublishedBatchBefore(LocalDateTime cutoff, int batchSize);

    void deleteAll(List<PostOutboxEvent> events);
}
