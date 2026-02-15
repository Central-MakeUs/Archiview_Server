package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByProviderAndProviderIdAndDeletedAtIsNull(User.OAuthProvider provider, String providerId);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByIdAndDeletedAtIsNull(UUID userId);
}
