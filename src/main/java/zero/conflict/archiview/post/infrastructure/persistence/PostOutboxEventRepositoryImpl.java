package zero.conflict.archiview.post.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostOutboxEventRepository;
import zero.conflict.archiview.post.domain.PostOutboxEvent;
import zero.conflict.archiview.post.domain.PostOutboxEventStatus;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostOutboxEventRepositoryImpl implements PostOutboxEventRepository {

    private static final List<PostOutboxEventStatus> RELAY_TARGET_STATUSES = List.of(
            PostOutboxEventStatus.PENDING,
            PostOutboxEventStatus.FAILED);

    private final PostOutboxEventJpaRepository postOutboxEventJpaRepository;

    @Override
    public PostOutboxEvent save(PostOutboxEvent event) {
        return postOutboxEventJpaRepository.save(event);
    }

    @Override
    public List<PostOutboxEvent> saveAll(List<PostOutboxEvent> events) {
        return postOutboxEventJpaRepository.saveAll(events);
    }

    @Override
    public List<PostOutboxEvent> findPendingBatch(int batchSize) {
        return postOutboxEventJpaRepository.findByStatusInOrderByIdAsc(
                RELAY_TARGET_STATUSES,
                PageRequest.of(0, batchSize));
    }
}
