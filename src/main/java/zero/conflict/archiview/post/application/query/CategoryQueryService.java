package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.infrastructure.CategoryPlaceReadRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final CategoryPlaceReadRepository categoryPlaceReadRepository;

    public CategoryQueryDto.CategoryListResponse getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return CategoryQueryDto.CategoryListResponse.from(categories);
    }

    public CategoryQueryDto.CategoryPlaceListResponse getPlacesByCategoryId(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DomainException(PostErrorCode.INVALID_CATEGORY_ID));

        return CategoryQueryDto.CategoryPlaceListResponse.from(
                categoryPlaceReadRepository.findPlaceSummariesByCategoryId(categoryId));
    }
}
