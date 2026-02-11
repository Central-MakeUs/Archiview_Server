package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;
import java.util.UUID;

public interface PostPlaceJpaRepository extends JpaRepository<PostPlace, Long> {
    List<PostPlace> findAllByEditorId(UUID editorId);

    void deleteAllByPost_Id(Long postId);

    List<PostPlace> findAllByPlace_IdIn(List<Long> placeIds);

    List<PostPlace> findAllByPlace_Id(Long placeId);

    List<PostPlace> findAllByPost_Id(Long postId);

    List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, Long placeId);

    List<PostPlace> findAllByEditorIdIn(List<UUID> editorIds);

    List<PostPlace> findAllByPostPlaceCategories_Category_Id(Long categoryId);
}
