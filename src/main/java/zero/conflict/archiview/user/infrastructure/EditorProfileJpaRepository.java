package zero.conflict.archiview.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.Optional;

public interface EditorProfileJpaRepository extends JpaRepository<EditorProfile, Long> {
    Optional<EditorProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByNickname(String nickname);
    boolean existsByInstagramId(String instagramId);
}
