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
