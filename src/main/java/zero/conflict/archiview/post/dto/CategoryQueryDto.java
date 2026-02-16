package zero.conflict.archiview.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.infrastructure.persistence.CategoryPlaceReadRepository;

import java.util.List;
public class CategoryQueryDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;

        public static CategoryResponse from(Category category) {
            return CategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryListResponse {
        private List<CategoryResponse> categories;

        public static CategoryListResponse of(List<CategoryResponse> categories) {
            return CategoryListResponse.builder()
                    .categories(categories)
                    .build();
        }

        public static CategoryListResponse from(List<Category> categories) {
            return CategoryListResponse.builder()
                    .categories(categories.stream().map(CategoryResponse::from).toList())
                    .build();
        }

        public static CategoryListResponse mock() {
            return CategoryListResponse.builder()
                    .categories(List.of(
                            CategoryResponse.builder().id(1L).name("카페").build(),
                            CategoryResponse.builder().id(2L).name("디저트").build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryPlaceResponse {
        private Long placeId;
        private String placeName;
        private String latestDescription;
        private String imageUrl;
        private Long viewCount;
        private Long saveCount;

        public static CategoryPlaceResponse from(CategoryPlaceReadRepository.CategoryPlaceSummaryProjection projection) {
            return CategoryPlaceResponse.builder()
                    .placeId(projection.getPlaceId())
                    .placeName(projection.getPlaceName())
                    .latestDescription(projection.getLatestDescription())
                    .imageUrl(null)
                    .viewCount(projection.getViewCount())
                    .saveCount(projection.getSaveCount())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryPlaceListResponse {
        private Long totalCount;
        private List<CategoryPlaceResponse> places;

        public static CategoryPlaceListResponse from(
                List<CategoryPlaceReadRepository.CategoryPlaceSummaryProjection> projections) {
            List<CategoryPlaceResponse> places = projections.stream().map(CategoryPlaceResponse::from).toList();
            return CategoryPlaceListResponse.builder()
                    .totalCount((long) places.size())
                    .places(places)
                    .build();
        }

        public static CategoryPlaceListResponse mock() {
            List<CategoryPlaceResponse> places = List.of(
                    CategoryPlaceResponse.builder()
                            .placeId(101L)
                            .placeName("성수 감성 카페")
                            .latestDescription("채광이 좋고 디저트가 맛있어요.")
                            .imageUrl("https://example.com/images/cafe-101.jpg")
                            .viewCount(1240L)
                            .saveCount(385L)
                            .build(),
                    CategoryPlaceResponse.builder()
                            .placeId(102L)
                            .placeName("연남 브런치 하우스")
                            .latestDescription("주말 오픈런 필수인 브런치 맛집.")
                            .imageUrl("https://example.com/images/brunch-102.jpg")
                            .viewCount(980L)
                            .saveCount(274L)
                            .build());
            return CategoryPlaceListResponse.builder()
                    .totalCount((long) places.size())
                    .places(places)
                    .build();
        }
    }
}
