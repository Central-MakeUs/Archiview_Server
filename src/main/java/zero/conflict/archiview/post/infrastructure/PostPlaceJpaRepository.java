package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlace;

public interface PostPlaceJpaRepository extends JpaRepository<PostPlace, Long> {
}
