package zero.conflict.archiview.post.application.editor.command.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.port.out.PostOutboxEventRepository;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostOutboxEvent;
import zero.conflict.archiview.post.domain.PostOutboxEventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostOutboxService {

    private final PostOutboxEventRepository postOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void appendPostCreatedEvent(Post post, List<Long> placeIds) {
        append(post, placeIds, PostOutboxEventType.POST_CREATED);
    }

    @Transactional
    public void appendPostUpdatedEvent(Post post, List<Long> placeIds) {
        append(post, placeIds, PostOutboxEventType.POST_UPDATED);
    }

    @Transactional
    public void appendPostDeletedEvent(Post post, List<Long> placeIds) {
        append(post, placeIds, PostOutboxEventType.POST_DELETED);
    }

    private void append(Post post, List<Long> placeIds, PostOutboxEventType eventType) {
        try {
            String eventId = UUID.randomUUID().toString();
            PostChangedOutboxPayload payload = new PostChangedOutboxPayload(
                    eventId,
                    post.getId(),
                    post.getEditorId(),
                    placeIds,
                    eventType,
                    LocalDateTime.now());
            String payloadJson = objectMapper.writeValueAsString(payload);
            postOutboxEventRepository.save(PostOutboxEvent.createPending(eventId, post.getId(), eventType, payloadJson));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize post outbox payload", e);
        }
    }
}
