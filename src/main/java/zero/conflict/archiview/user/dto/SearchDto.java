package zero.conflict.archiview.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SearchDto {

    public enum Tab {
        ALL,
        PLACE,
        EDITOR
    }

    public enum KeywordType {
        URL,
        KEYWORD
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SearchResponse")
    public static class Response {
        private String query;
        private Tab tab;
        private long placeCount;
        private long editorCount;
        private boolean hasMorePlaces;
        private boolean hasMoreEditors;
        private List<PlaceCard> places;
        private List<EditorCard> editors;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceCard {
        private Long placeId;
        private String placeName;
        private String imageUrl;
        private String summary;
        private String addressName;
        private String roadAddressName;
        private Long saveCount;
        private Long viewCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime latestUpdatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditorCard {
        private UUID editorId;
        private String nickname;
        private String instagramId;
        private String introduction;
        private String profileImageUrl;
        private List<String> hashtags;
        private boolean following;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime latestUpdatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "최근 검색어 목록 응답")
    public static class RecentListResponse {
        private int totalCount;
        private List<RecentItem> histories;

        public static RecentListResponse from(List<RecentItem> histories) {
            return RecentListResponse.builder()
                    .totalCount(histories.size())
                    .histories(histories)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentItem {
        private Long historyId;
        private String keyword;
        private String displayKeyword;
        private KeywordType keywordType;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime searchedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 키워드 목록 응답")
    public static class RecommendationListResponse {
        private int totalCount;
        private List<RecommendationItem> keywords;

        public static RecommendationListResponse from(List<RecommendationItem> keywords) {
            return RecommendationListResponse.builder()
                    .totalCount(keywords.size())
                    .keywords(keywords)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationItem {
        private String keyword;
        private long count;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime latestUsedAt;
    }
}
