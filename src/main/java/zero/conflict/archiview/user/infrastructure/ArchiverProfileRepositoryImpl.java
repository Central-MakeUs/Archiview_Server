package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.ArchiverProfileRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;

@Repository
@RequiredArgsConstructor
public class ArchiverProfileRepositoryImpl implements ArchiverProfileRepository {

    private final ArchiverProfileJpaRepository archiverProfileJpaRepository;

    @Override
    public ArchiverProfile save(ArchiverProfile archiverProfile) {
        return archiverProfileJpaRepository.save(archiverProfile);
    }
}
