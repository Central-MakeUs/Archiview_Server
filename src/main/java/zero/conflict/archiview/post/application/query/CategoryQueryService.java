package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public CategoryQueryDto.CategoryListResponse getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return CategoryQueryDto.CategoryListResponse.from(categories);
    }
}
