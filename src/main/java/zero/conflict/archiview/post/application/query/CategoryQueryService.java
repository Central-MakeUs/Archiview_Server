package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.presentation.dto.CategoryQueryDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public CategoryQueryDto.CategoryListResponse getCategories() {
        List<CategoryQueryDto.CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(CategoryQueryService::toResponse)
                .collect(Collectors.toList());

        return CategoryQueryDto.CategoryListResponse.of(categories);
    }

    private static CategoryQueryDto.CategoryResponse toResponse(Category category) {
        return CategoryQueryDto.CategoryResponse.of(category.getId(), category.getName());
    }
}
