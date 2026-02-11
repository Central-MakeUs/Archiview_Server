package zero.conflict.archiview.post.application.editor.command.event;

import zero.conflict.archiview.post.domain.PostOutboxEventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostChangedOutboxPayload(
        String eventId,
        Long postId,
        UUID editorId,
        List<Long> placeIds,
        PostOutboxEventType eventType,
        LocalDateTime occurredAt
) {
}
