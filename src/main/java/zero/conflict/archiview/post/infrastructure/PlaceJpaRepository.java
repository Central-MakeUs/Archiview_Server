package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.Place;

import java.util.Optional;
import java.util.UUID;

public interface PlaceJpaRepository extends JpaRepository<Place, UUID> {
    Page<Place> findAllByOrderByViewCountDesc(Pageable pageable);

    Optional<Place> findByPosition_LatitudeAndPosition_Longitude(Double latitude, Double longitude);
}
