package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.PostPlace;

@Repository
@RequiredArgsConstructor
public class PostPlaceRepositoryImpl implements PostPlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    @Override
    public PostPlace save(PostPlace postPlace) {
        return null;
    }
}
