package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository {

    Follow save(Follow follow);

    Optional<Follow> findById(Long id);

    Optional<Follow> findByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    boolean existsByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    void deleteByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    List<Follow> findAllByArchiverId(UUID archiverId);

    List<Follow> findAllByEditorId(UUID editorId);
}
