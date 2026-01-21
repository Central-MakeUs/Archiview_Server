package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;

public interface PostPlaceRepository {

    PostPlace save(PostPlace postPlace);

    List<PostPlace> findAllByEditorId(Long editorId);
}
