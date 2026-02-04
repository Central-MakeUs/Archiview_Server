package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.FollowRepository;
import zero.conflict.archiview.user.domain.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final FollowJpaRepository followJpaRepository;

    @Override
    public Follow save(Follow follow) {
        return followJpaRepository.save(follow);
    }

    @Override
    public Optional<Follow> findById(Long id) {
        return followJpaRepository.findById(id);
    }

    @Override
    public Optional<Follow> findByArchiverIdAndEditorId(UUID archiverId, UUID editorId) {
        return followJpaRepository.findByArchiverIdAndEditorId(archiverId, editorId);
    }

    @Override
    public boolean existsByArchiverIdAndEditorId(UUID archiverId, UUID editorId) {
        return followJpaRepository.existsByArchiverIdAndEditorId(archiverId, editorId);
    }

    @Override
    public void deleteByArchiverIdAndEditorId(UUID archiverId, UUID editorId) {
        followJpaRepository.deleteByArchiverIdAndEditorId(archiverId, editorId);
    }

    @Override
    public List<Follow> findAllByArchiverId(UUID archiverId) {
        return followJpaRepository.findAllByArchiverId(archiverId);
    }

    @Override
    public List<Follow> findAllByEditorId(UUID editorId) {
        return followJpaRepository.findAllByEditorId(editorId);
    }
}
