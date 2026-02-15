package zero.conflict.archiview.user.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Optional<User> findByIdIncludingDeleted(UUID id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(User.OAuthProvider provider, String providerId) {
        return userJpaRepository.findByProviderAndProviderIdAndDeletedAtIsNull(provider, providerId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return userJpaRepository.existsByIdAndDeletedAtIsNull(userId);
    }

    @Override
    public void deleteById(UUID userId) {
        userJpaRepository.deleteById(userId);
    }
}
