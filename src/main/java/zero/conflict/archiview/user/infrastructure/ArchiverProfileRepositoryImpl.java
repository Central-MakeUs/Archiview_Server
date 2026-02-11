package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ArchiverProfileRepositoryImpl implements ArchiverProfileRepository {

    private final ArchiverProfileJpaRepository archiverProfileJpaRepository;

    @Override
    public ArchiverProfile save(ArchiverProfile archiverProfile) {
        return archiverProfileJpaRepository.save(archiverProfile);
    }

    @Override
    public Optional<ArchiverProfile> findByUserId(UUID userId) {
        return archiverProfileJpaRepository.findByUser_Id(userId);
    }
}
