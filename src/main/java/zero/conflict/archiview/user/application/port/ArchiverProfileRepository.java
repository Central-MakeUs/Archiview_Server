package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.ArchiverProfile;

import java.util.Optional;
import java.util.UUID;

public interface ArchiverProfileRepository {
    ArchiverProfile save(ArchiverProfile archiverProfile);

    Optional<ArchiverProfile> findByUserId(UUID userId);
}
