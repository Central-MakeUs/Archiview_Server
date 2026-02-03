package zero.conflict.archiview.user.application.port;

import zero.conflict.archiview.user.domain.ArchiverProfile;

public interface ArchiverProfileRepository {
    ArchiverProfile save(ArchiverProfile archiverProfile);
}
