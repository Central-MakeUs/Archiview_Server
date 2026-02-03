package zero.conflict.archiview.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.Optional;
import java.util.UUID;

public interface EditorProfileJpaRepository extends JpaRepository<EditorProfile, Long> {
    Optional<EditorProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    boolean existsByNickname(String nickname);
    boolean existsByInstagramId(String instagramId);
}
