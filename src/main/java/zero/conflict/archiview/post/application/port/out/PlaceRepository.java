package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;

import java.util.Optional;

public interface PlaceRepository {

    Place save(Place place);

    Optional<Place> findByPosition(Position position);
}
