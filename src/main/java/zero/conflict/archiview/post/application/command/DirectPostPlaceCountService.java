package zero.conflict.archiview.post.application.command;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.UUID;

@Service
@ConditionalOnProperty(
        name = "post-place.count-write-back.enabled",
        havingValue = "false",
        matchIfMissing = true)
public class DirectPostPlaceCountService implements PostPlaceCountService {

    @Override
    public void increaseViewCount(PostPlace postPlace, UUID actorId) {
        postPlace.increaseViewCount(actorId);
    }

    @Override
    public Long increaseInstagramInflowCount(PostPlace postPlace, UUID actorId) {
        postPlace.increaseInstagramInflowCount(actorId);
        return defaultZero(postPlace.getInstagramInflowCount());
    }

    @Override
    public Long increaseDirectionCount(PostPlace postPlace, UUID actorId) {
        postPlace.increaseDirectionCount(actorId);
        return defaultZero(postPlace.getDirectionCount());
    }

    @Override
    public void increaseSaveCount(PostPlace postPlace, UUID actorId) {
        postPlace.increaseSaveCount(actorId);
    }

    @Override
    public void decreaseSaveCount(PostPlace postPlace, UUID actorId) {
        postPlace.decreaseSaveCount(actorId);
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
