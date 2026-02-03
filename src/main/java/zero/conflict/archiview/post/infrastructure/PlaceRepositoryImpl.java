package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;

import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    @Override
    public Place save(Place place) {
        return placeJpaRepository.save(place);
    }

    @Override
    public Optional<Place> findById(Long id) {
        return placeJpaRepository.findById(id);
    }

    @Override
    public List<Place> findTopByViewCount(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return placeJpaRepository.findAllByOrderByViewCountDesc(PageRequest.of(0, limit)).getContent();
    }

    @Override
    public Optional<Place> findByPosition(Position position) {
        return placeJpaRepository.findByPosition_LatitudeAndPosition_Longitude(
                position.getLatitude(), position.getLongitude());
    }

    @Override
    public List<Place> findAllByIds(List<Long> ids) {
        return placeJpaRepository.findAllById(ids);
    }
}
