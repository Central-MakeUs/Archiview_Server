package zero.conflict.archiview.post.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostOutboxEventRepository;
import zero.conflict.archiview.post.domain.PostOutboxEvent;
import zero.conflict.archiview.post.domain.PostOutboxEventStatus;

import java.time.LocalDateTime;
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
    public List<PostOutboxEvent> findRelayBatch(LocalDateTime now, int batchSize) {
        return postOutboxEventJpaRepository.findByStatusInAndNextRetryAtLessThanEqualOrderByIdAsc(
                RELAY_TARGET_STATUSES,
                now,
                PageRequest.of(0, batchSize));
    }

    @Override
    public List<PostOutboxEvent> findPublishedBatchBefore(LocalDateTime cutoff, int batchSize) {
        return postOutboxEventJpaRepository.findByStatusAndPublishedAtBeforeOrderByIdAsc(
                PostOutboxEventStatus.PUBLISHED,
                cutoff,
                PageRequest.of(0, batchSize));
    }

    @Override
    public void deleteAll(List<PostOutboxEvent> events) {
        postOutboxEventJpaRepository.deleteAllInBatch(events);
    }
}
