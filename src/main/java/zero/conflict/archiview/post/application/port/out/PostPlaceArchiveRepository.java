package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlaceArchive;

import java.util.List;
import java.util.UUID;

public interface PostPlaceArchiveRepository {

    PostPlaceArchive save(PostPlaceArchive postPlaceArchive);

    boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    List<PostPlaceArchive> findAllByArchiverIdOrderByCreatedAtDesc(UUID archiverId);
}
