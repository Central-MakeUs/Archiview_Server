package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;
import java.util.UUID;

public interface PostPlaceRepository {

    PostPlace save(PostPlace postPlace);

    void deleteAllByPostId(Long postId);

    java.util.Optional<PostPlace> findById(Long postPlaceId);

    List<PostPlace> findAllByPlaceIds(List<Long> placeIds);

    List<PostPlace> findAllByPlaceId(Long placeId);

    List<PostPlace> findAllByEditorId(UUID editorId);

    List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, Long placeId);

    List<PostPlace> findAllByEditorIds(List<UUID> editorIds);
}
