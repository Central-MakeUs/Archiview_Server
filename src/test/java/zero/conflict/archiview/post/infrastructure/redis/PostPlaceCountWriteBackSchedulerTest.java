package zero.conflict.archiview.post.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import zero.conflict.archiview.post.infrastructure.persistence.PostPlaceCountBulkUpdater;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostPlaceCountWriteBackScheduler 테스트")
class PostPlaceCountWriteBackSchedulerTest {

    @InjectMocks
    private PostPlaceCountWriteBackScheduler scheduler;

    @Mock
    private PostPlaceCountRedisRepository redisRepository;

    @Mock
    private PostPlaceCountBulkUpdater bulkUpdater;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "batchSize", 200);
    }

    @Test
    @DisplayName("flush 시 델타를 한번에 bulk 업데이트한다")
    void flush_bulkUpdate() {
        List<PostPlaceCountDelta> deltas = List.of(
                new PostPlaceCountDelta(1L, 1L, 0L, 0L, 0L),
                new PostPlaceCountDelta(2L, 0L, 1L, 2L, 3L));
        given(redisRepository.popDeltas(200)).willReturn(deltas);
        given(bulkUpdater.applyDeltas(deltas)).willReturn(2);

        scheduler.flush();

        verify(bulkUpdater).applyDeltas(deltas);
        verify(redisRepository, never()).requeue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("bulk 업데이트 실패 시 배치 전체를 requeue한다")
    void flush_bulkUpdateFails_requeueAll() {
        PostPlaceCountDelta first = new PostPlaceCountDelta(10L, 1L, 0L, 0L, 0L);
        PostPlaceCountDelta second = new PostPlaceCountDelta(11L, 0L, -1L, 0L, 0L);
        List<PostPlaceCountDelta> deltas = List.of(first, second);
        given(redisRepository.popDeltas(200)).willReturn(deltas);
        given(bulkUpdater.applyDeltas(anyList())).willThrow(new RuntimeException("db failed"));

        scheduler.flush();

        verify(redisRepository).requeue(first);
        verify(redisRepository).requeue(second);
    }
}
