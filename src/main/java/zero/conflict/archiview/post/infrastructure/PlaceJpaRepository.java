package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.Place;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
}
