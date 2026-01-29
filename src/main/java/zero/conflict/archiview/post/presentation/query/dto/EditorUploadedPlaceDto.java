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

        public static PlaceCardResponse of(Long placeId, String placeName, String imageUrl, String description,
                Stats stats) {
            return PlaceCardResponse.builder()
                    .placeId(placeId)
                    .placeName(placeName)
                    .placeImageUrl(imageUrl)
                    .editorSummary(description)
                    .stats(stats)
                    .build();
        }
    }

    /*
     * ## 3단계: 에디터 인사이트 로직 구현 (완료)
     * - [x] `PostQueryService`에 인사이트 요약(`getInsightSummary`) 구현 <!-- id: 40 -->
     * - [x] `PostQueryService`에 인사이트 장소 목록(`getInsightPlaces`) 구현 <!-- id: 41 -->
     * - [x] `EditorPostQueryController` 서비스 연동 <!-- id: 42 -->
     * - [x] 인사이트 기간 필터링(`Period`) 로직 구현 <!-- id: 43 -->
     *
     * ## 4단계: 검증 및 테스트 (완료)
     * - [x] 인사이트 API 통합 테스트 확인 <!-- id: 50 -->
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stats {
        private Long saveCount;
        private Long viewCount;
        private Long instagramInflowCount;
        private Long directionCount;

        public static Stats from(long saveCount, long viewCount, long instagramInflowCount, long directionCount) {
            return Stats.builder()
                    .saveCount(saveCount)
                    .viewCount(viewCount)
                    .instagramInflowCount(instagramInflowCount)
                    .directionCount(directionCount)
                    .build();
        }
    }
}
