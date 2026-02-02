package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findByName(String name);

    Optional<Category> findById(UUID id);

    List<Category> findAll();
}
