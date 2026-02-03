package zero.conflict.archiview.post.infrastructure;

import zero.conflict.archiview.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Post, Long> {
}
