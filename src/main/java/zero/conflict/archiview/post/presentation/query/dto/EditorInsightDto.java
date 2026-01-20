package zero.conflict.archiview.post.presentation.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceCardListResponse {
        private Period period;
        private PlaceSort sort;
        private List<PlaceCardResponse> places;

        public static PlaceCardListResponse empty(Period period, PlaceSort sort) {
            return PlaceCardListResponse.builder()
                    .period(period)
                    .sort(sort)
                    .places(Collections.emptyList())
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
        private Period period;
        private String placeName;
        private String placeImageUrl;
        private String editorSummary;
        private Stats stats;
        private LocalDateTime updatedAt;

        public static PlaceDetailResponse empty(Long placeId, Period period) {
            return PlaceDetailResponse.builder()
                    .placeId(placeId)
                    .period(period)
                    .build();
        }
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
