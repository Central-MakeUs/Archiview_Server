package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(User.OAuthProvider provider, String providerId);
    boolean existsByEmail(String email);
}
