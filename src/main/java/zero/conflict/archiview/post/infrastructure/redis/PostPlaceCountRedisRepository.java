package zero.conflict.archiview.post.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostPlaceCountRedisRepository {

    private static final String DIRTY_SET_KEY = "post-place:count:dirty";
    private static final DefaultRedisScript<Long> INCREMENT_AND_MARK_DIRTY_SCRIPT = new DefaultRedisScript<>(
            """
                    local delta = redis.call('HINCRBY', KEYS[1], ARGV[1], ARGV[2])
                    redis.call('SADD', KEYS[2], ARGV[3])
                    return delta
                    """,
            Long.class);
    private static final DefaultRedisScript<List> GET_AND_DELETE_HASH_SCRIPT = new DefaultRedisScript<>(
            """
                    local values = redis.call('HGETALL', KEYS[1])
                    redis.call('DEL', KEYS[1])
                    return values
                    """,
            List.class);

    private final StringRedisTemplate redisTemplate;

    public long incrementDelta(Long postPlaceId, PostPlaceCountMetric metric, long delta) {
        Long updated = redisTemplate.execute(
                INCREMENT_AND_MARK_DIRTY_SCRIPT,
                List.of(deltaKey(postPlaceId), DIRTY_SET_KEY),
                metric.field(),
                String.valueOf(delta),
                String.valueOf(postPlaceId));
        return updated == null ? 0L : updated;
    }

    public long getDelta(Long postPlaceId, PostPlaceCountMetric metric) {
        Object raw = redisTemplate.opsForHash().get(deltaKey(postPlaceId), metric.field());
        if (raw == null) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public List<PostPlaceCountDelta> popDeltas(int batchSize) {
        List<String> ids = redisTemplate.opsForSet().pop(DIRTY_SET_KEY, batchSize);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<PostPlaceCountDelta> deltas = new ArrayList<>();
        for (String idString : ids) {
            Long postPlaceId = parseId(idString);
            if (postPlaceId == null) {
                continue;
            }
            PostPlaceCountDelta delta = popDelta(postPlaceId);
            if (!delta.isEmpty()) {
                deltas.add(delta);
            }
        }
        return deltas;
    }

    public void requeue(PostPlaceCountDelta delta) {
        if (delta.viewDelta() != 0L) {
            incrementDelta(delta.postPlaceId(), PostPlaceCountMetric.VIEW, delta.viewDelta());
        }
        if (delta.saveDelta() != 0L) {
            incrementDelta(delta.postPlaceId(), PostPlaceCountMetric.SAVE, delta.saveDelta());
        }
        if (delta.instagramInflowDelta() != 0L) {
            incrementDelta(delta.postPlaceId(), PostPlaceCountMetric.INSTAGRAM_INFLOW, delta.instagramInflowDelta());
        }
        if (delta.directionDelta() != 0L) {
            incrementDelta(delta.postPlaceId(), PostPlaceCountMetric.DIRECTION, delta.directionDelta());
        }
    }

    private PostPlaceCountDelta popDelta(Long postPlaceId) {
        List<Object> values = redisTemplate.execute(GET_AND_DELETE_HASH_SCRIPT, List.of(deltaKey(postPlaceId)));
        if (values == null || values.isEmpty()) {
            return new PostPlaceCountDelta(postPlaceId, 0L, 0L, 0L, 0L);
        }

        long view = 0L;
        long save = 0L;
        long instagram = 0L;
        long direction = 0L;

        for (int i = 0; i + 1 < values.size(); i += 2) {
            String field = String.valueOf(values.get(i));
            long value = parseLong(values.get(i + 1));
            if (PostPlaceCountMetric.VIEW.field().equals(field)) {
                view = value;
            } else if (PostPlaceCountMetric.SAVE.field().equals(field)) {
                save = value;
            } else if (PostPlaceCountMetric.INSTAGRAM_INFLOW.field().equals(field)) {
                instagram = value;
            } else if (PostPlaceCountMetric.DIRECTION.field().equals(field)) {
                direction = value;
            }
        }

        return new PostPlaceCountDelta(postPlaceId, view, save, instagram, direction);
    }

    private Long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private long parseLong(Object value) {
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String deltaKey(Long postPlaceId) {
        return "post-place:count:delta:" + postPlaceId;
    }
}
