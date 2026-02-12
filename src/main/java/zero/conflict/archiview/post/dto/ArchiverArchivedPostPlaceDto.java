package zero.conflict.archiview.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Schema(description = "아카이버 아카이브한 장소카드(postPlace) 조회 DTO")
public class ArchiverArchivedPostPlaceDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverArchivedPostPlaceListResponse")
    public static class ListResponse {
        private Long totalCount;
        private List<ArchivedPostPlaceResponse> postPlaces;

        public static ListResponse from(List<ArchivedPostPlaceResponse> postPlaces) {
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
                            ArchivedPostPlaceResponse.builder()
                                    .postPlaceId(901L)
                                    .placeId(301L)
                                    .placeName("성수 감성 카페")
                                    .description("창가 좌석이 좋아요.")
                                    .imageUrl("https://picsum.photos/400/300?random=71")
                                    .saveCount(52L)
                                    .viewCount(210L)
                                    .lastModifiedAt(LocalDateTime.now().minusDays(1))
                                    .archivedAt(LocalDateTime.now().minusHours(3))
                                    .build(),
                            ArchivedPostPlaceResponse.builder()
                                    .postPlaceId(902L)
                                    .placeId(302L)
                                    .placeName("연남 브런치 하우스")
                                    .description("브런치 메뉴가 다양해요.")
                                    .imageUrl("https://picsum.photos/400/300?random=72")
                                    .saveCount(34L)
                                    .viewCount(120L)
                                    .lastModifiedAt(LocalDateTime.now().minusDays(2))
                                    .archivedAt(LocalDateTime.now().minusDays(1))
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArchivedPostPlaceResponse {
        @Schema(description = "postPlace ID", example = "901")
        private Long postPlaceId;
        @Schema(description = "place ID", example = "301")
        private Long placeId;
        @Schema(description = "장소명", example = "성수 감성 카페")
        private String placeName;
        @Schema(description = "장소 설명", example = "창가 좌석이 좋아요.")
        private String description;
        @Schema(description = "장소 이미지 URL")
        private String imageUrl;
        @Schema(description = "저장수", example = "52")
        private Long saveCount;
        @Schema(description = "조회수", example = "210")
        private Long viewCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "최종 수정 시각", example = "2026-02-09 22:10:00")
        private LocalDateTime lastModifiedAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "아카이브 시각", example = "2026-02-10 19:20:00")
        private LocalDateTime archivedAt;
    }
}
