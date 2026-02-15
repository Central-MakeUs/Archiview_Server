package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;

import java.util.Optional;
import java.util.UUID;

public interface ArchiverProfileJpaRepository extends JpaRepository<ArchiverProfile, Long> {
    Optional<ArchiverProfile> findByUser_Id(UUID userId);
    void deleteByUser_Id(UUID userId);
}
