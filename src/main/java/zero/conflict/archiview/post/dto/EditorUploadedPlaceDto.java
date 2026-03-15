package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.Collections;
import java.util.List;

public class EditorUploadedPlaceDto {

    @Schema(description = "업로드 장소 목록 정렬 기준. UPDATED는 최근 수정순, CREATED는 생성순")
    public enum PlaceSort {
        UPDATED,
        CREATED
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "EditorUploadedPlaceListResponse", description = "에디터가 업로드한 장소 목록 응답")
    public static class ListResponse {
        @Schema(description = "조건에 맞는 장소 카드 목록", example = "[]")
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
    @Schema(name = "EditorUploadedPlaceCardResponse", description = "업로드한 장소 카드 1건")
    public static class PlaceCardResponse {
        @Schema(description = "장소 ID", example = "101")
        private Long placeId;
        @Schema(description = "장소명", example = "성수동 카페거리 샘플")
        private String placeName;
        @Schema(description = "외부 장소 URL", example = "https://place.map.kakao.com/123456")
        private String placeUrl;
        @Schema(description = "장소 전화번호", example = "02-1234-5678")
        private String phoneNumber;
        @Schema(description = "장소 대표 이미지 URL", example = "https://picsum.photos/400/300?random=11")
        private String placeImageUrl;
        @Schema(description = "에디터가 작성한 장소 한줄 설명", example = "성수동에서 가장 인기 있는 카페입니다.")
        private String editorSummary;
        @Schema(description = "장소 통계 정보")
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

        public static PlaceCardResponse from(
                Place place,
                PostPlace latestPostPlace,
                Stats stats) {
            Place resolvedPlace =
                    place != null ? place : latestPostPlace.getPlace();
            return PlaceCardResponse.builder()
                    .placeId(resolvedPlace.getId())
                    .placeName(resolvedPlace.getName())
                    .placeUrl(resolvedPlace.getPlaceUrl())
                    .phoneNumber(resolvedPlace.getPhoneNumber())
                    .placeImageUrl(latestPostPlace.getImageUrl())
                    .editorSummary(latestPostPlace.getDescription())
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
    @Schema(name = "EditorUploadedPlaceStats", description = "장소 통계 정보")
    public static class Stats {
        @Schema(description = "저장 수", example = "300")
        private Long saveCount;
        @Schema(description = "조회 수", example = "1000")
        private Long viewCount;
        @Schema(description = "인스타그램 유입 수", example = "500")
        private Long instagramInflowCount;
        @Schema(description = "길찾기 클릭 수", example = "150")
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
