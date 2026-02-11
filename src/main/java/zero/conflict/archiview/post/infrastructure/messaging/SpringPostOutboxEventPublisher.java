package zero.conflict.archiview.post.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.post.application.editor.command.event.PostOutboxPublishedEvent;
import zero.conflict.archiview.post.application.port.out.PostOutboxEventPublisher;
import zero.conflict.archiview.post.domain.PostOutboxEvent;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringPostOutboxEventPublisher implements PostOutboxEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PostOutboxEvent event) {
        log.info(
                "Publishing post outbox event. outboxEventId={}, aggregateId={}, eventType={}",
                event.getId(),
                event.getAggregateId(),
                event.getEventType());

        applicationEventPublisher.publishEvent(new PostOutboxPublishedEvent(
                event.getId(),
                event.getEventId(),
                event.getAggregateId(),
                event.getEventType(),
                event.getPayload(),
                LocalDateTime.now()));
    }
}
