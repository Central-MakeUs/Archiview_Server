package zero.conflict.archiview.post.application.command;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.infrastructure.redis.PostPlaceCountMetric;
import zero.conflict.archiview.post.infrastructure.redis.PostPlaceCountRedisRepository;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "post-place.count-write-back.enabled", havingValue = "true")
public class RedisPostPlaceCountService implements PostPlaceCountService {

    private final PostPlaceCountRedisRepository redisRepository;

    public RedisPostPlaceCountService(PostPlaceCountRedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void increaseViewCount(PostPlace postPlace, UUID actorId) {
        if (isOwner(postPlace, actorId)) {
            return;
        }
        redisRepository.incrementDelta(postPlace.getId(), PostPlaceCountMetric.VIEW, 1L);
    }

    @Override
    public Long increaseInstagramInflowCount(PostPlace postPlace, UUID actorId) {
        long base = defaultZero(postPlace.getInstagramInflowCount());
        if (isOwner(postPlace, actorId)) {
            return base + redisRepository.getDelta(postPlace.getId(), PostPlaceCountMetric.INSTAGRAM_INFLOW);
        }
        long delta = redisRepository.incrementDelta(postPlace.getId(), PostPlaceCountMetric.INSTAGRAM_INFLOW, 1L);
        return base + delta;
    }

    @Override
    public Long increaseDirectionCount(PostPlace postPlace, UUID actorId) {
        long base = defaultZero(postPlace.getDirectionCount());
        if (isOwner(postPlace, actorId)) {
            return base + redisRepository.getDelta(postPlace.getId(), PostPlaceCountMetric.DIRECTION);
        }
        long delta = redisRepository.incrementDelta(postPlace.getId(), PostPlaceCountMetric.DIRECTION, 1L);
        return base + delta;
    }

    @Override
    public void increaseSaveCount(PostPlace postPlace, UUID actorId) {
        if (isOwner(postPlace, actorId)) {
            return;
        }
        redisRepository.incrementDelta(postPlace.getId(), PostPlaceCountMetric.SAVE, 1L);
    }

    @Override
    public void decreaseSaveCount(PostPlace postPlace, UUID actorId) {
        if (isOwner(postPlace, actorId)) {
            return;
        }
        long base = defaultZero(postPlace.getSaveCount());
        long pending = redisRepository.getDelta(postPlace.getId(), PostPlaceCountMetric.SAVE);
        if (base + pending <= 0L) {
            return;
        }
        redisRepository.incrementDelta(postPlace.getId(), PostPlaceCountMetric.SAVE, -1L);
    }

    private boolean isOwner(PostPlace postPlace, UUID actorId) {
        return postPlace.getEditorId() != null && postPlace.getEditorId().equals(actorId);
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
