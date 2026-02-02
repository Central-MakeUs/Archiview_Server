package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    @Override
    public Place save(Place place) {
        return placeJpaRepository.save(place);
    }

    @Override
    public Optional<Place> findByPosition(Position position) {
        return placeJpaRepository.findByPosition_LatitudeAndPosition_Longitude(
                position.getLatitude(), position.getLongitude());
    }

    @Override
    public List<Place> findAllByIds(List<UUID> ids) {
        return placeJpaRepository.findAllById(ids);
    }
}
