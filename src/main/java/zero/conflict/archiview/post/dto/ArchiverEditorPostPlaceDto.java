package zero.conflict.archiview.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.PostPlace;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Schema(description = "아카이버용 에디터 업로드 장소(postPlace) 조회 DTO")
public class ArchiverEditorPostPlaceDto {

    public enum Sort {
        LATEST,
        OLDEST
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverEditorPostPlaceListResponse")
    public static class ListResponse {
        private Long totalCount;
        private List<PostPlaceResponse> postPlaces;

        public static ListResponse from(List<PostPlaceResponse> postPlaces) {
            return ListResponse.builder()
                    .totalCount((long) postPlaces.size())
                    .postPlaces(postPlaces)
                    .build();
        }

        public static ListResponse empty() {
            return ListResponse.builder()
                    .totalCount(0L)
                    .postPlaces(Collections.emptyList())
                    .build();
        }

        public static ListResponse mock() {
            return ListResponse.builder()
                    .totalCount(2L)
                    .postPlaces(List.of(
                            PostPlaceResponse.builder()
                                    .postPlaceId(1001L)
                                    .placeName("성수 감성 카페")
                                    .description("채광 좋은 창가 자리가 좋아요.")
                                    .saveCount(32L)
                                    .viewCount(210L)
                                    .lastModifiedAt(LocalDateTime.now().minusDays(1))
                                    .imageUrl("https://picsum.photos/400/300?random=31")
                                    .build(),
                            PostPlaceResponse.builder()
                                    .postPlaceId(1002L)
                                    .placeName("연남 파스타 바")
                                    .description("면 식감이 좋은 숨은 맛집입니다.")
                                    .saveCount(11L)
                                    .viewCount(95L)
                                    .lastModifiedAt(LocalDateTime.now().minusDays(3))
                                    .imageUrl("https://picsum.photos/400/300?random=32")
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverEditorPostPlaceResponse")
    public static class PostPlaceResponse {
        @Schema(description = "postPlace ID", example = "1001")
        private Long postPlaceId;
        @Schema(description = "장소명", example = "성수 감성 카페")
        private String placeName;
        @Schema(description = "장소 설명", example = "채광 좋은 창가 자리가 좋아요.")
        private String description;
        @Schema(description = "저장수", example = "32")
        private Long saveCount;
        @Schema(description = "조회수", example = "210")
        private Long viewCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "최종 수정 시각", example = "2026-02-09 22:10:00")
        private LocalDateTime lastModifiedAt;
        @Schema(description = "장소 이미지 URL")
        private String imageUrl;

        public static PostPlaceResponse from(PostPlace postPlace, LocalDateTime lastModifiedAt) {
            return PostPlaceResponse.builder()
                    .postPlaceId(postPlace.getId())
                    .placeName(postPlace.getPlace() != null ? postPlace.getPlace().getName() : null)
                    .description(postPlace.getDescription())
                    .saveCount(postPlace.getSaveCount() == null ? 0L : postPlace.getSaveCount())
                    .viewCount(postPlace.getViewCount() == null ? 0L : postPlace.getViewCount())
                    .lastModifiedAt(lastModifiedAt)
                    .imageUrl(postPlace.getImageUrl())
                    .build();
        }
    }
}
