package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.EditorBlock;

import java.util.List;
import java.util.UUID;

public interface EditorBlockRepository {

    EditorBlock save(EditorBlock editorBlock);

    boolean existsByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    void deleteByArchiverIdAndEditorId(UUID archiverId, UUID editorId);

    List<EditorBlock> findAllByArchiverId(UUID archiverId);
}
