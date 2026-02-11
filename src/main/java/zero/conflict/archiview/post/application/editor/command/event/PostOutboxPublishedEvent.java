package zero.conflict.archiview.post.application.editor.command.event;

import zero.conflict.archiview.post.domain.PostOutboxEventType;

import java.time.LocalDateTime;

public record PostOutboxPublishedEvent(
        Long outboxEventId,
        String eventId,
        Long aggregateId,
        PostOutboxEventType eventType,
        String payload,
        LocalDateTime publishedAt
) {
}
