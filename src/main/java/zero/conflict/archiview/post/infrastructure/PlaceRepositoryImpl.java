package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    @Override
    public Place save(Place place) {
        return null;
    }

    @Override
    public Optional<Place> findByPosition(Position position) {
        return Optional.empty();
    }
}
