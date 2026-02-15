package zero.conflict.archiview.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.auth.domain.DevLoginRedirectAllowlist;

import java.util.Optional;

public interface DevLoginRedirectAllowlistJpaRepository extends JpaRepository<DevLoginRedirectAllowlist, Long> {

    boolean existsByEmailIgnoreCaseAndEnabledTrue(String email);

    Optional<DevLoginRedirectAllowlist> findByEmailIgnoreCaseAndEnabledTrue(String email);
}
