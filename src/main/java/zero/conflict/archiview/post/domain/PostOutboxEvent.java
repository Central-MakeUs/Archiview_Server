package zero.conflict.archiview.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "post_outbox_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_outbox_event_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_post_outbox_status_id", columnList = "status,id"),
                @Index(name = "idx_post_outbox_aggregate_id", columnList = "aggregate_id")
        })
public class PostOutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_id", nullable = false, length = 36, updatable = false)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostOutboxEventType eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostOutboxEventStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(length = 1000)
    private String lastError;

    private LocalDateTime publishedAt;

    public static PostOutboxEvent createPending(Long aggregateId, PostOutboxEventType eventType, String payload) {
        return createPending(UUID.randomUUID().toString(), aggregateId, eventType, payload);
    }

    public static PostOutboxEvent createPending(
            String eventId,
            Long aggregateId,
            PostOutboxEventType eventType,
            String payload) {
        return PostOutboxEvent.builder()
                .aggregateId(aggregateId)
                .eventId(eventId)
                .eventType(eventType)
                .payload(payload)
                .status(PostOutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = PostOutboxEventStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void markFailed(String errorMessage) {
        this.status = PostOutboxEventStatus.FAILED;
        this.retryCount = this.retryCount + 1;
        this.lastError = truncate(errorMessage);
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() <= 1000) {
            return message;
        }
        return message.substring(0, 1000);
    }
}
