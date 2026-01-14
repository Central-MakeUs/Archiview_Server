package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(User.OAuthProvider provider, String providerId);
    boolean existsByEmail(String email);
}

