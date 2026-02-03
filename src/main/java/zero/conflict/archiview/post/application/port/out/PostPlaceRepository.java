package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;
import java.util.UUID;

public interface PostPlaceRepository {

    PostPlace save(PostPlace postPlace);

    void deleteAllByPostId(UUID postId);

    List<PostPlace> findAllByEditorId(UUID editorId);

    List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, UUID placeId);
}
