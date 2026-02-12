package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceCategory;

import java.util.List;

public class EditorMapDto {

    public enum MapFilter {
        ALL, NEARBY
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 지도 장소 핀 응답")
    public static class PlacePinResponse {
        @Schema(description = "장소 ID")
        private Long placeId;
        @Schema(description = "장소명")
        private String name;
        @Schema(description = "장소 URL")
        private String placeUrl;
        @Schema(description = "전화번호")
        private String phoneNumber;
        @Schema(description = "위도")
        private Double latitude;
        @Schema(description = "경도")
        private Double longitude;
        @Schema(description = "장소 카테고리 목록")
        private List<String> categories;

        public static PlacePinResponse from(
                Place place,
                List<PostPlace> postPlaces) {
            List<String> categoryNames = postPlaces.stream()
                    .flatMap(postPlace -> postPlace.getPostPlaceCategories().stream())
                    .map(PostPlaceCategory::getCategory)
                    .filter(category -> category != null && category.getName() != null)
                    .map(category -> category.getName())
                    .distinct()
                    .toList();
            return PlacePinResponse.builder()
                    .placeId(place.getId())
                    .name(place.getName())
                    .placeUrl(place.getPlaceUrl())
                    .phoneNumber(place.getPhoneNumber())
                    .latitude(place.getPosition().getLatitude())
                    .longitude(place.getPosition().getLongitude())
                    .categories(categoryNames)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "EditorMapResponse", description = "에디터 지도 장소 목록 응답")
    public static class Response {
        @Schema(description = "필터링된 장소 핀 목록")
        private List<PlacePinResponse> pins;

        public static Response from(List<PlacePinResponse> pins) {
            return Response.builder()
                    .pins(pins)
                    .build();
        }

        public static Response empty() {
            return Response.builder()
                    .pins(List.of())
                    .build();
        }

        public static Response mock() {
            // ... (기존 mock 유지)
            return Response.builder()
                    .pins(List.of(
                            PlacePinResponse.builder()
                                    .placeId(1L)
                                    .name("성수동 힙플레이스")
                                    .latitude(37.5445)
                                    .longitude(127.0560)
                                    .categories(List.of("카페", "인테리어"))
                                    .build(),
                            PlacePinResponse.builder()
                                    .placeId(2L)
                                    .name("연남동 숨은 맛집")
                                    .latitude(37.5615)
                                    .longitude(126.9249)
                                    .categories(List.of("맛집", "일식"))
                                    .build()))
                    .build();
        }
    }
}
