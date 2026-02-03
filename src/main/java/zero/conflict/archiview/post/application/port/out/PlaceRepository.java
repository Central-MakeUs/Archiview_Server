package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface PlaceRepository {

    Place save(Place place);

    java.util.Optional<Place> findById(java.util.UUID id);

    List<Place> findTopByViewCount(int limit);

    Optional<Place> findByPosition(Position position);

    List<Place> findAllByIds(List<UUID> ids);
}
