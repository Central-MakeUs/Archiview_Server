package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findByName(String name);

    Optional<Category> findById(Long id);

    List<Category> findAll();
}
