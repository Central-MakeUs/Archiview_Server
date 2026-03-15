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

    @Schema(description = "검색 탭. ALL은 통합, PLACE는 장소 중심, EDITOR는 에디터 중심")
    public enum Tab {
        ALL,
        PLACE,
        EDITOR
    }

    @Schema(description = "검색어 유형. URL은 인스타 URL, KEYWORD는 일반 키워드")
    public enum KeywordType {
        URL,
        KEYWORD
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SearchResponse", description = "아카이버 검색 응답")
    public static class Response {
        @Schema(description = "실제 검색에 사용된 원본 검색어", example = "성수")
        private String query;
        @Schema(description = "현재 조회 탭", example = "ALL")
        private Tab tab;
        @Schema(description = "장소 검색 결과 개수", example = "12")
        private long placeCount;
        @Schema(description = "에디터 검색 결과 개수", example = "3")
        private long editorCount;
        @Schema(description = "장소 결과를 더 불러올 수 있는지 여부", example = "false")
        private boolean hasMorePlaces;
        @Schema(description = "에디터 결과를 더 불러올 수 있는지 여부", example = "false")
        private boolean hasMoreEditors;
        @Schema(description = "장소 검색 결과 카드 목록")
        private List<PlaceCard> places;
        @Schema(description = "에디터 검색 결과 카드 목록")
        private List<EditorCard> editors;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SearchPlaceCard", description = "검색 결과의 장소 카드")
    public static class PlaceCard {
        @Schema(description = "장소 ID", example = "101")
        private Long placeId;
        @Schema(description = "장소명", example = "성수 감성 카페")
        private String placeName;
        @Schema(description = "대표 이미지 URL", example = "https://picsum.photos/400/300?random=31")
        private String imageUrl;
        @Schema(description = "장소 요약 설명", example = "창가 좌석이 인기인 카페")
        private String summary;
        @Schema(description = "지번 주소", example = "서울 성동구 성수동2가 123-45")
        private String addressName;
        @Schema(description = "도로명 주소", example = "서울 성동구 아차산로 123")
        private String roadAddressName;
        @Schema(description = "저장 수", example = "52")
        private Long saveCount;
        @Schema(description = "조회 수", example = "210")
        private Long viewCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "최근 수정 시각", example = "2026-03-15 12:30:00")
        private LocalDateTime latestUpdatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SearchEditorCard", description = "검색 결과의 에디터 카드")
    public static class EditorCard {
        @Schema(description = "에디터 userId", example = "00000000-0000-0000-0000-000000000101")
        private UUID editorId;
        @Schema(description = "에디터 닉네임", example = "감도높은에디터")
        private String nickname;
        @Schema(description = "에디터 인스타그램 ID", example = "archiview_editor")
        private String instagramId;
        @Schema(description = "에디터 한줄 소개", example = "서울의 공간을 기록합니다.")
        private String introduction;
        @Schema(description = "에디터 프로필 이미지 URL")
        private String profileImageUrl;
        @Schema(description = "에디터 대표 해시태그 목록", example = "[\"#성수카페\", \"#전시공간\"]")
        private List<String> hashtags;
        @Schema(description = "현재 로그인한 사용자의 팔로우 여부", example = "true")
        private boolean following;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "최근 활동 시각", example = "2026-03-15 12:30:00")
        private LocalDateTime latestUpdatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "최근 검색어 목록 응답")
    public static class RecentListResponse {
        @Schema(description = "최근 검색어 총 개수", example = "3")
        private int totalCount;
        @Schema(description = "최근 검색어 목록")
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
    @Schema(name = "RecentSearchItem", description = "최근 검색어 1건")
    public static class RecentItem {
        @Schema(description = "검색 히스토리 ID", example = "10")
        private Long historyId;
        @Schema(description = "원본 검색어", example = "https://www.instagram.com/p/ABC123/")
        private String keyword;
        @Schema(description = "UI 표시용 검색어", example = "성수")
        private String displayKeyword;
        @Schema(description = "검색어 유형", example = "KEYWORD")
        private KeywordType keywordType;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "검색 시각", example = "2026-03-15 12:30:00")
        private LocalDateTime searchedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 키워드 목록 응답")
    public static class RecommendationListResponse {
        @Schema(description = "추천 키워드 총 개수", example = "5")
        private int totalCount;
        @Schema(description = "추천 키워드 목록")
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
    @Schema(name = "RecommendationItem", description = "추천 키워드 1건")
    public static class RecommendationItem {
        @Schema(description = "추천 키워드", example = "성수")
        private String keyword;
        @Schema(description = "최근 사용/노출 횟수", example = "24")
        private long count;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "가장 최근 사용 시각", example = "2026-03-15 12:30:00")
        private LocalDateTime latestUsedAt;
    }
}
