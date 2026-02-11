package zero.conflict.archiview.global.infra.event;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.global.domain.EventConsumeCheckpoint;

public interface EventConsumeCheckpointJpaRepository extends JpaRepository<EventConsumeCheckpoint, Long> {
}
