package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.EditorBlockRepository;
import zero.conflict.archiview.user.domain.EditorBlock;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EditorBlockRepositoryImpl implements EditorBlockRepository {

    private final EditorBlockJpaRepository editorBlockJpaRepository;

    @Override
    public EditorBlock save(EditorBlock editorBlock) {
        return editorBlockJpaRepository.save(editorBlock);
    }

    @Override
    public boolean existsByArchiverIdAndEditorId(UUID archiverId, UUID editorId) {
        return editorBlockJpaRepository.existsByArchiverIdAndEditorId(archiverId, editorId);
    }

    @Override
    public void deleteByArchiverIdAndEditorId(UUID archiverId, UUID editorId) {
        editorBlockJpaRepository.deleteByArchiverIdAndEditorId(archiverId, editorId);
    }

    @Override
    public List<EditorBlock> findAllByArchiverId(UUID archiverId) {
        return editorBlockJpaRepository.findAllByArchiverId(archiverId);
    }
}
