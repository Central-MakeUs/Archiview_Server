package zero.conflict.archiview.user.application.port.out;

import zero.conflict.archiview.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(User.OAuthProvider provider, String providerId);
    boolean existsByEmail(String email);

    boolean existsByUserId(UUID userId);
}
