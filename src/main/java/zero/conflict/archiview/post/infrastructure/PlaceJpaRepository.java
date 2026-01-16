package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.Place;

import java.math.BigDecimal;
import java.util.Optional;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByPosition_LatitudeAndPosition_Longitude(BigDecimal latitude, BigDecimal longitude);
}
