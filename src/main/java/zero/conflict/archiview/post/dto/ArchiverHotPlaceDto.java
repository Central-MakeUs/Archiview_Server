package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Schema(description = "아카이버용 핫플레이스 DTO")
public class ArchiverHotPlaceDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverHotPlaceListResponse", description = "아카이버 핫플레이스 목록 응답")
    public static class ListResponse {
        private List<PlaceCardResponse> places;

        public static ListResponse from(List<PlaceCardResponse> places) {
            return ListResponse.builder()
                    .places(places)
                    .build();
        }

        public static ListResponse empty() {
            return ListResponse.builder()
                    .places(Collections.emptyList())
                    .build();
        }

        public static ListResponse mock() {
            return ListResponse.builder()
                    .places(List.of(
                            PlaceCardResponse.builder()
                                    .placeId(201L)
                                    .name("성수 핫플 카페")
                                    .imageUrl("https://picsum.photos/400/300?random=21")
                                    .viewCount(1200L)
                                    .build(),
                            PlaceCardResponse.builder()
                                    .placeId(202L)
                                    .name("연남동 맛집")
                                    .imageUrl("https://picsum.photos/400/300?random=22")
                                    .viewCount(980L)
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverHotPlaceCard", description = "아카이버 핫플레이스 카드")
    public static class PlaceCardResponse {
        @Schema(description = "장소 ID", example = "00000000-0000-0000-0000-000000000201")
        private Long placeId;
        @Schema(description = "장소명", example = "성수 핫플 카페")
        private String name;
        @Schema(description = "대표 이미지 URL")
        private String imageUrl;
        @Schema(description = "조회수", example = "1200")
        private Long viewCount;

        public static PlaceCardResponse from(
                zero.conflict.archiview.post.domain.Place place,
                zero.conflict.archiview.post.domain.PostPlace latestPostPlace) {
            return PlaceCardResponse.builder()
                    .placeId(place.getId())
                    .name(place.getName())
                    .imageUrl(latestPostPlace != null ? latestPostPlace.getImageUrl() : null)
                    .viewCount(place.getViewCount() == null ? 0L : place.getViewCount())
                    .build();
        }
    }
}
