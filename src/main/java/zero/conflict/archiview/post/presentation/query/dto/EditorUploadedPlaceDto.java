package zero.conflict.archiview.post.presentation.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

public class EditorUploadedPlaceDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private List<PlaceCardResponse> places;

        public static ListResponse empty() {
            return ListResponse.builder()
                    .places(Collections.emptyList())
                    .build();
        }

        public static ListResponse mock() {
            return ListResponse.builder()
                    .places(List.of(
                            PlaceCardResponse.builder()
                                    .placeId(101L)
                                    .placeName("성수동 카페거리 샘플")
                                    .placeImageUrl("https://picsum.photos/400/300?random=11")
                                    .editorSummary("성수동에서 가장 인기 있는 카페입니다.")
                                    .stats(Stats.builder()
                                            .viewCount(1000L)
                                            .saveCount(300L)
                                            .instagramInflowCount(500L)
                                            .directionCount(150L)
                                            .build())
                                    .build(),
                            PlaceCardResponse.builder()
                                    .placeId(102L)
                                    .placeName("연남동 베이커리")
                                    .placeImageUrl("https://picsum.photos/400/300?random=12")
                                    .editorSummary("직접 구운 빵이 유명한 곳이에요.")
                                    .stats(Stats.builder()
                                            .viewCount(850L)
                                            .saveCount(210L)
                                            .instagramInflowCount(320L)
                                            .directionCount(90L)
                                            .build())
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceCardResponse {
        private Long placeId;
        private String placeName;
        private String placeImageUrl;
        private String editorSummary;
        private Stats stats;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stats {
        private Long saveCount;
        private Long viewCount;
        private Long instagramInflowCount;
        private Long directionCount;
    }
}
