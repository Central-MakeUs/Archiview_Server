package zero.conflict.archiview.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(User.OAuthProvider provider, String providerId);
    boolean existsByEmail(String email);
}

