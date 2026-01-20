package zero.conflict.archiview.post.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CategoryQueryDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;

        public static CategoryResponse of(Long id, String name) {
            return CategoryResponse.builder()
                    .id(id)
                    .name(name)
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
    }
}
