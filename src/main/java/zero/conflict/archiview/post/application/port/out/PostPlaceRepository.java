package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PostPlaceRepository {

    PostPlace save(PostPlace postPlace);

    void deleteAllByPostId(Long postId);

    void deleteAllByIdIn(List<Long> postPlaceIds);

    void markDeletedAllByPostId(Long postId, UUID actorId, LocalDateTime deletedAt);

    java.util.Optional<PostPlace> findById(Long postPlaceId);

    List<PostPlace> findAllByIds(List<Long> postPlaceIds);

    List<PostPlace> findAllByPlaceIds(List<Long> placeIds);

    List<PostPlace> findAllByPlaceId(Long placeId);

    List<PostPlace> findAllByPostId(Long postId);

    List<PostPlace> findAllByEditorId(UUID editorId);

    List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, Long placeId);

    List<PostPlace> findAllByEditorIds(List<UUID> editorIds);

    List<PostPlace> findAllByCategoryId(Long categoryId);

    List<PostPlace> findAll();
}
