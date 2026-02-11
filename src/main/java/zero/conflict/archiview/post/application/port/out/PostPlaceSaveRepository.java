package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlaceSave;

import java.util.List;
import java.util.UUID;

public interface PostPlaceSaveRepository {

    PostPlaceSave save(PostPlaceSave postPlaceSave);

    boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    List<PostPlaceSave> findAllByArchiverIdOrderByCreatedAtDesc(UUID archiverId);
}
