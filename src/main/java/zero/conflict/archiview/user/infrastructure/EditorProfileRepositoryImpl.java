package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EditorProfileRepositoryImpl implements EditorProfileRepository {

    private final EditorProfileJpaRepository editorProfileJpaRepository;

    @Override
    public EditorProfile save(EditorProfile editorProfile) {
        return editorProfileJpaRepository.save(editorProfile);
    }

    @Override
    public Optional<EditorProfile> findByUserId(UUID userId) {
        return editorProfileJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<EditorProfile> findById(Long id) {
        return editorProfileJpaRepository.findById(id);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return editorProfileJpaRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return editorProfileJpaRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByInstagramId(String instagramId) {
        return editorProfileJpaRepository.existsByInstagramId(instagramId);
    }
}
