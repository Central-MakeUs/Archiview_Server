package zero.conflict.archiview.global.infra.event;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.domain.EventConsumeCheckpoint;

@Component
@RequiredArgsConstructor
public class EventIdempotencyGuard {

    private final EventConsumeCheckpointJpaRepository checkpointRepository;

    @Transactional
    public boolean tryAcquire(String consumerName, String eventId) {
        try {
            checkpointRepository.save(EventConsumeCheckpoint.builder()
                    .consumerName(consumerName)
                    .eventId(eventId)
                    .build());
            return true;
        } catch (DataIntegrityViolationException ignored) {
            return false;
        }
    }
}
