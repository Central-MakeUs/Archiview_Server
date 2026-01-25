package zero.conflict.archiview.post.presentation.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Address;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class EditorInsightDto {

    public enum Period {
        ALL,
        MONTH,
        WEEK
    }

    public enum PlaceSort {
        RECENT,
        MOST_VIEWED,
        MOST_SAVED,
        MOST_INSTAGRAM,
        MOST_DIRECTIONS
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryResponse {
        private String editorNickname;
        private Long totalPlaceCount;
        private Period period;
        private Long sharedPlaceCount;
        private Long instagramInflowCount;
        private Long saveCount;
        private Long viewCount;

        public static SummaryResponse empty(Period period) {
            return SummaryResponse.builder()
                    .period(period)
                    .build();
        }

        public static SummaryResponse mock(Period period) {
            return SummaryResponse.builder()
                    .editorNickname("아카이브 마스터")
                    .totalPlaceCount(128L)
                    .period(period)
                    .sharedPlaceCount(45L)
                    .instagramInflowCount(1250L)
                    .saveCount(890L)
                    .viewCount(5600L)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceCardListResponse {
        private PlaceSort sort;
        private List<PlaceCardResponse> places;

        public static PlaceCardListResponse empty(PlaceSort sort) {
            return PlaceCardListResponse.builder()
                    .sort(sort)
                    .places(Collections.emptyList())
                    .build();
        }

        public static PlaceCardListResponse mock(PlaceSort sort) {
            return PlaceCardListResponse.builder()
                    .sort(sort)
                    .places(List.of(
                            PlaceCardResponse.builder()
                                    .placeId(1L)
                                    .placeName("샘플 카페 성수")
                                    .placeImageUrl("https://picsum.photos/400/300?random=1")
                                    .editorSummary("성수동에서 가장 힙한 분위기의 카페입니다.")
                                    .stats(Stats.builder().viewCount(120L).saveCount(45L).build())
                                    .updatedAt(LocalDateTime.now().minusDays(1))
                                    .build(),
                            PlaceCardResponse.builder()
                                    .placeId(2L)
                                    .placeName("연남동 맛집")
                                    .placeImageUrl("https://picsum.photos/400/300?random=2")
                                    .editorSummary("웨이팅이 아깝지 않은 정통 일식당입니다.")
                                    .stats(Stats.builder().viewCount(350L).saveCount(120L).build())
                                    .updatedAt(LocalDateTime.now().minusDays(2))
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
        private LocalDateTime updatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceDetailResponse {
        private Long placeId;
        private String placeName;
        private String placeImageUrl;
        private Long editorTotal;
        private Address address;
        private String nearestStationWalkTime;
        private Stats stats;

        private List<PostPlaceDetailResponse> postPlaces;

        public static PlaceDetailResponse empty(Long placeId) {
            return PlaceDetailResponse.builder()
                    .placeId(placeId)
                    .postPlaces(Collections.emptyList())
                    .build();
        }

        public static PlaceDetailResponse mock(Long placeId) {
            return PlaceDetailResponse.builder()
                    .placeId(placeId)
                    .placeName("모의 장소 상세")
                    .placeImageUrl("https://picsum.photos/800/600?random=10")
                    .editorTotal(5L)
                    .address(Address.of("서울특별시 성동구 아차산로 123", "2층 201호", "04782"))
                    .nearestStationWalkTime("성수역 도보 5분")
                    .stats(Stats.builder()
                            .viewCount(1500L)
                            .saveCount(450L)
                            .instagramInflowCount(800L)
                            .directionCount(200L)
                            .build())
                    .postPlaces(List.of(
                            PostPlaceDetailResponse.builder()
                                    .editorName("에디터A")
                                    .editorInstagramId("editor_a")
                                    .postUrl("https://instagram.com/p/sample1")
                                    .postHashTag("#성수카페 #감성")
                                    .description("채광이 너무 좋은 곳이에요.")
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostPlaceDetailResponse {
        private String editorName;
        private String editorInstagramId;
        private String postUrl;
        private String postHashTag;
        private String description;
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
