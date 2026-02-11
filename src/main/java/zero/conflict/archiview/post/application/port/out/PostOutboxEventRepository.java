package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostOutboxEvent;

import java.util.List;

public interface PostOutboxEventRepository {

    PostOutboxEvent save(PostOutboxEvent event);

    List<PostOutboxEvent> saveAll(List<PostOutboxEvent> events);

    List<PostOutboxEvent> findPendingBatch(int batchSize);
}
