package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EditorProfileJpaRepository extends JpaRepository<EditorProfile, Long> {
    Optional<EditorProfile> findByUser_Id(UUID userId);

    List<EditorProfile> findAllByUser_IdIn(List<UUID> userIds);

    boolean existsByUser_Id(UUID userId);

    boolean existsByNickname(String nickname);

    boolean existsByInstagramId(String instagramId);

    void deleteByUser_Id(UUID userId);
}
