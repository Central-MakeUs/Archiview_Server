package zero.conflict.archiview.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.auth.domain.DevLoginRedirectAllowlist;

public interface DevLoginRedirectAllowlistJpaRepository extends JpaRepository<DevLoginRedirectAllowlist, Long> {

    boolean existsByEmailIgnoreCaseAndEnabledTrue(String email);
}

