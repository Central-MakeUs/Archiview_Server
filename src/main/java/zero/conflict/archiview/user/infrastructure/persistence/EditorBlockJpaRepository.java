package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.EditorBlock;

import java.util.List;
import java.util.UUID;

public interface EditorBlockJpaRepository extends JpaRepository<EditorBlock, Long> {

    boolean existsByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    void deleteByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    List<EditorBlock> findAllByArchiverId(UUID archiverId);
}
