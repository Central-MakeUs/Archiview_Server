package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostOutboxEvent;
import zero.conflict.archiview.post.domain.PostOutboxEventStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PostOutboxEventJpaRepository extends JpaRepository<PostOutboxEvent, Long> {

    List<PostOutboxEvent> findByStatusInAndNextRetryAtLessThanEqualOrderByIdAsc(
            List<PostOutboxEventStatus> statuses,
            LocalDateTime now,
            Pageable pageable);

    List<PostOutboxEvent> findByStatusAndPublishedAtBeforeOrderByIdAsc(
            PostOutboxEventStatus status,
            LocalDateTime cutoff,
            Pageable pageable);
}
