package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;

public interface PostPlaceJpaRepository extends JpaRepository<PostPlace, Long> {
    List<PostPlace> findAllByEditorId(Long editorId);

    List<PostPlace> findAllByEditorIdAndPlaceId(Long editorId, Long placeId);
}
