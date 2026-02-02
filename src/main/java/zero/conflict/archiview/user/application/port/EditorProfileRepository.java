package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.Optional;

public interface EditorProfileRepository {
    EditorProfile save(EditorProfile editorProfile);
    Optional<EditorProfile> findByUserId(Long userId);
    Optional<EditorProfile> findById(Long id);
    boolean existsByUserId(Long userId);
    boolean existsByNickname(String nickname);
    boolean existsByInstagramId(String instagramId);
}
