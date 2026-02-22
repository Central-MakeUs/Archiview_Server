package zero.conflict.archiview.post.application.archiver.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final PostPlaceRepository postPlaceRepository;
    private final ArchiverVisibilityService archiverVisibilityService;

    public CategoryQueryDto.CategoryListResponse getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return CategoryQueryDto.CategoryListResponse.from(categories);
    }

    public CategoryQueryDto.CategoryPlaceListResponse getPlacesByCategoryId(Long categoryId) {
        return getPlacesByCategoryId(categoryId, null);
    }

    public CategoryQueryDto.CategoryPlaceListResponse getPlacesByCategoryId(Long categoryId, UUID archiverId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DomainException(PostErrorCode.INVALID_CATEGORY_ID));

        List<PostPlace> postPlaces = postPlaceRepository.findAllByCategoryId(categoryId);
        if (archiverId != null) {
            postPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                    postPlaces,
                    archiverVisibilityService.getVisibilityFilter(archiverId));
        }
        if (postPlaces.isEmpty()) {
            return CategoryQueryDto.CategoryPlaceListResponse.builder()
                    .totalCount(0L)
                    .places(List.of())
                    .build();
        }

        Map<Long, List<PostPlace>> byPlace = postPlaces.stream()
                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

        List<CategoryQueryDto.CategoryPlaceResponse> places = byPlace.values().stream()
                .map(this::toCategoryPlaceResponse)
                .sorted(Comparator.comparing(CategoryQueryDto.CategoryPlaceResponse::getPlaceId).reversed())
                .toList();

        return CategoryQueryDto.CategoryPlaceListResponse.builder()
                .totalCount((long) places.size())
                .places(places)
                .build();
    }

    private CategoryQueryDto.CategoryPlaceResponse toCategoryPlaceResponse(List<PostPlace> postPlaces) {
        PostPlace latestPostPlace = postPlaces.stream()
                .max(Comparator.comparing(
                        postPlace -> postPlace.getLastModifiedAt() != null ? postPlace.getLastModifiedAt()
                                : postPlace.getCreatedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();

        long viewCount = defaultZero(latestPostPlace.getPlace() != null ? latestPostPlace.getPlace().getViewCount() : null);
        long saveCount = postPlaces.stream()
                .map(PostPlace::getSaveCount)
                .mapToLong(this::defaultZero)
                .sum();

        return CategoryQueryDto.CategoryPlaceResponse.builder()
                .placeId(latestPostPlace.getPlace().getId())
                .placeName(latestPostPlace.getPlace().getName())
                .latestDescription(latestPostPlace.getDescription())
                .imageUrl(latestPostPlace.getImageUrl())
                .viewCount(viewCount)
                .saveCount(saveCount)
                .build();
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
