package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostOutboxEvent;
import zero.conflict.archiview.post.domain.PostOutboxEventStatus;

import java.util.List;

public interface PostOutboxEventJpaRepository extends JpaRepository<PostOutboxEvent, Long> {

    List<PostOutboxEvent> findByStatusInOrderByIdAsc(List<PostOutboxEventStatus> statuses, Pageable pageable);
}
