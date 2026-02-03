package zero.conflict.archiview.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;

import java.util.UUID;

public interface ArchiverProfileJpaRepository extends JpaRepository<ArchiverProfile, UUID> {
}
