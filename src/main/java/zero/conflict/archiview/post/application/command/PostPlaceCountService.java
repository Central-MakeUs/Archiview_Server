package zero.conflict.archiview.post.application.command;

import zero.conflict.archiview.post.domain.PostPlace;

import java.util.UUID;

public interface PostPlaceCountService {

    void increaseViewCount(PostPlace postPlace, UUID actorId);

    Long increaseInstagramInflowCount(PostPlace postPlace, UUID actorId);

    Long increaseDirectionCount(PostPlace postPlace, UUID actorId);

    void increaseSaveCount(PostPlace postPlace, UUID actorId);

    void decreaseSaveCount(PostPlace postPlace, UUID actorId);
}
