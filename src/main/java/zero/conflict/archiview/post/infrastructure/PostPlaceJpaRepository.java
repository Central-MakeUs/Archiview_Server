package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;
import java.util.UUID;

public interface PostPlaceJpaRepository extends JpaRepository<PostPlace, UUID> {
    List<PostPlace> findAllByEditorId(UUID editorId);

    void deleteAllByPost_Id(UUID postId);

    List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, UUID placeId);
}
