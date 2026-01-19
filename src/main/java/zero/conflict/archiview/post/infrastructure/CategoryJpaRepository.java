package zero.conflict.archiview.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.Category;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
}
