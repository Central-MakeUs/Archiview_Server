package zero.conflict.archiview.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowJpaRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    boolean existsByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    void deleteByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    List<Follow> findAllByArchiverId(UUID archiverId);

    List<Follow> findAllByEditorId(UUID editorId);
}
