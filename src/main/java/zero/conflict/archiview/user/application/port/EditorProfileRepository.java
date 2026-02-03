package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.Optional;
import java.util.UUID;

public interface EditorProfileRepository {
    EditorProfile save(EditorProfile editorProfile);
    Optional<EditorProfile> findByUserId(UUID userId);
    Optional<EditorProfile> findById(Long id);
    boolean existsByUserId(UUID userId);
    boolean existsByNickname(String nickname);
    boolean existsByInstagramId(String instagramId);
}
