package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zero.conflict.archiview.user.domain.EditorProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EditorProfileJpaRepository extends JpaRepository<EditorProfile, Long> {
    @Query("select ep from EditorProfile ep join fetch ep.user where ep.user.id = :userId")
    Optional<EditorProfile> findByUser_Id(@Param("userId") UUID userId);

    @Query("select ep from EditorProfile ep join fetch ep.user where ep.user.id in :userIds")
    List<EditorProfile> findAllByUser_IdIn(@Param("userIds") List<UUID> userIds);

    boolean existsByUser_Id(UUID userId);

    boolean existsByNickname(String nickname);

    boolean existsByInstagramId(String instagramId);

    void deleteByUser_Id(UUID userId);
}
