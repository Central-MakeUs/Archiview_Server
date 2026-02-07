package zero.conflict.archiview.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Address;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceCategory;
import zero.conflict.archiview.user.domain.EditorProfile;

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
        private Long instagramInflowCount;
        private Long saveCount;
        private Long viewCount;

        public static SummaryResponse of(String nickname, long totalCount, long instagramCount,
                long saveCount, long viewCount, Period period) {
            return SummaryResponse.builder()
                    .editorNickname(nickname)
                    .totalPlaceCount(totalCount)
                    .instagramInflowCount(instagramCount)
                    .saveCount(saveCount)
                    .viewCount(viewCount)
                    .period(period)
                    .build();
        }

        public static SummaryResponse from(EditorProfile editorProfile,
                long totalCount, long instagramCount, long saveCount, long viewCount, Period period) {
            return SummaryResponse.builder()
                    .editorNickname(editorProfile.getNickname())
                    .totalPlaceCount(totalCount)
                    .instagramInflowCount(instagramCount)
                    .saveCount(saveCount)
                    .viewCount(viewCount)
                    .period(period)
                    .build();
        }

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

        public static PlaceCardListResponse of(PlaceSort sort, List<PlaceCardResponse> places) {
            return PlaceCardListResponse.builder()
                    .sort(sort)
                    .places(places)
                    .build();
        }

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
                                    .categories(List.of("카페", "디저트"))
                                    .hashTags(List.of("#성수카페", "#감성"))
                                    .stats(Stats.builder().viewCount(120L).saveCount(45L).build())
                                    .updatedAt(LocalDateTime.now().minusDays(1))
                                    .build(),
                            PlaceCardResponse.builder()
                                    .placeId(2L)
                                    .placeName("연남동 맛집")
                                    .placeImageUrl("https://picsum.photos/400/300?random=2")
                                    .editorSummary("웨이팅이 아깝지 않은 정통 일식당입니다.")
                                    .categories(List.of("맛집"))
                                    .hashTags(List.of("#연남동", "#맛집"))
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
        @Schema(description = "장소 ID", example = "101")
        private Long placeId;
        @Schema(description = "장소명", example = "아카이브 성수")
        private String placeName;
        @Schema(description = "장소 URL")
        private String placeUrl;
        @Schema(description = "전화번호")
        private String phoneNumber;
        @Schema(description = "장소 대표 이미지 URL", example = "https://picsum.photos/400/300")
        private String placeImageUrl;
        @Schema(description = "에디터 요약", example = "성수동 감성 카페의 정석")
        private String editorSummary;
        @Schema(description = "장소 카테고리 목록", example = "[\"카페\", \"디저트\"]")
        private List<String> categories;
        @Schema(description = "장소 해시태그 1~3개", example = "[\"#성수카페\", \"#감성\", \"#데이트코스\"]")
        private List<String> hashTags;
        private Stats stats;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "업데이트 일시", example = "2024-01-29 10:00:00")
        private LocalDateTime updatedAt;

        public static PlaceCardResponse of(Place place, String summary,
                String imageUrl, Stats stats, LocalDateTime updatedAt) {
            return PlaceCardResponse.builder()
                    .placeId(place.getId())
                    .placeName(place.getName())
                    .placeUrl(place.getPlaceUrl())
                    .phoneNumber(place.getPhoneNumber())
                    .placeImageUrl(imageUrl)
                    .editorSummary(summary)
                    .stats(stats)
                    .updatedAt(updatedAt)
                    .build();
        }

        public static PlaceCardResponse from(
                Place place,
                PostPlace postPlace,
                Stats stats,
                LocalDateTime updatedAt) {
            List<String> categories = postPlace.getPostPlaceCategories().stream()
                    .map(PostPlaceCategory::getCategory)
                    .filter(category -> category != null && category.getName() != null)
                    .map(category -> category.getName())
                    .distinct()
                    .toList();
            Post post = postPlace.getPost();
            return PlaceCardResponse.builder()
                    .placeId(place.getId())
                    .placeName(place.getName())
                    .placeUrl(place.getPlaceUrl())
                    .phoneNumber(place.getPhoneNumber())
                    .placeImageUrl(postPlace.getImageUrl())
                    .editorSummary(postPlace.getDescription())
                    .categories(categories)
                    .hashTags(post != null ? post.getHashTags() : null)
                    .stats(stats)
                    .updatedAt(updatedAt)
                    .build();
        }
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

        public static PlaceDetailResponse of(Long placeId, List<PostPlaceDetailResponse> postPlaces) {
            return PlaceDetailResponse.builder()
                    .placeId(placeId)
                    .postPlaces(postPlaces)
                    .build();
        }

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
                    .address(Address.of("서울특별시 성동구 성수동 123-45", "서울특별시 성동구 아차산로 123"))
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
                                    .postHashTags(List.of("#성수카페", "#감성"))
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
        @Schema(description = "장소-게시글 매핑 ID", example = "1")
        private Long postPlaceId;
        @Schema(description = "에디터 이름", example = "아카이브 마스터")
        private String editorName;
        @Schema(description = "에디터 인스타그램 ID", example = "archiview_master")
        private String editorInstagramId;
        @Schema(description = "인스타그램 게시글 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String postUrl;
        @Schema(description = "게시글 해시태그 1~3개", example = "[\"#성수카페\", \"#감성레벨\", \"#데이트코스\"]")
        private List<String> postHashTags;
        @Schema(description = "에디터의 장소 설명", example = "분위기가 너무 좋고 커피가 맛있어요.")
        private String description;
        @Schema(description = "장소 카테고리 목록", example = "[\"카페\", \"데이트\"]")
        private List<String> categories;

        public static PostPlaceDetailResponse of(Long postPlaceId, String editorName, String editorInstagramId,
                String postUrl,
                List<String> postHashTags, String description, List<String> categories) {
            return PostPlaceDetailResponse.builder()
                    .postPlaceId(postPlaceId)
                    .editorName(editorName)
                    .editorInstagramId(editorInstagramId)
                    .postUrl(postUrl)
                    .postHashTags(postHashTags)
                    .description(description)
                    .categories(categories)
                    .build();
        }

        public static PostPlaceDetailResponse from(
                EditorProfile editorProfile,
                PostPlace postPlace) {
            Post post = postPlace.getPost();
            List<String> categories = postPlace.getPostPlaceCategories().stream()
                    .map(PostPlaceCategory::getCategory)
                    .filter(category -> category != null)
                    .map(category -> category.getName())
                    .toList();
            return PostPlaceDetailResponse.builder()
                    .postPlaceId(postPlace.getId())
                    .editorName(editorProfile.getNickname())
                    .editorInstagramId(editorProfile.getInstagramId())
                    .postUrl(post != null ? post.getUrl() : null)
                    .postHashTags(post != null ? post.getHashTags() : null)
                    .description(postPlace.getDescription())
                    .categories(categories)
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
