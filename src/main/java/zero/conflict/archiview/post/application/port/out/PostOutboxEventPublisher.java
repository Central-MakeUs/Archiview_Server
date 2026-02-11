package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostOutboxEvent;

public interface PostOutboxEventPublisher {

    void publish(PostOutboxEvent event);
}
