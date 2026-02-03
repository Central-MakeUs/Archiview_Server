package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Schema(description = "아카이버용 장소 상세 DTO")
public class ArchiverPlaceDetailDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverPlaceDetailResponse", description = "아카이버 장소 상세 응답")
    public static class Response {
        private PlaceResponse place;
        private List<PostPlaceResponse> postPlaces;

        public static Response from(PlaceResponse place, List<PostPlaceResponse> postPlaces) {
            return Response.builder()
                    .place(place)
                    .postPlaces(postPlaces)
                    .build();
        }

        public static Response empty(PlaceResponse place) {
            return Response.builder()
                    .place(place)
                    .postPlaces(Collections.emptyList())
                    .build();
        }

        public static Response mock() {
            return Response.builder()
                            .place(PlaceResponse.builder()
                            .placeId(301L)
                            .name("성수 핫플 카페")
                            .roadAddress("서울특별시 성동구 아차산로 123")
                            .detailAddress("2층 201호")
                            .zipCode("04782")
                            .latitude(37.5445)
                            .longitude(127.0560)
                            .nearestStationWalkTime("성수역 도보 5분")
                            .viewCount(1200L)
                            .build())
                            .postPlaces(List.of(
                            PostPlaceResponse.builder()
                                    .postPlaceId(401L)
                                    .postId(501L)
                                    .description("분위기가 너무 좋고 커피가 맛있어요.")
                                    .imageUrl("https://picsum.photos/400/300?random=31")
                                    .categoryNames(List.of("카페", "디저트"))
                                    .build(),
                            PostPlaceResponse.builder()
                                    .postPlaceId(402L)
                                    .postId(502L)
                                    .description("조용해서 대화하기 좋아요.")
                                    .imageUrl("https://picsum.photos/400/300?random=32")
                                    .categoryNames(List.of("카페"))
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverPlaceDetailPlace", description = "장소 정보")
    public static class PlaceResponse {
        @Schema(description = "장소 ID", example = "00000000-0000-0000-0000-000000000301")
        private Long placeId;
        @Schema(description = "장소명", example = "성수 핫플 카페")
        private String name;
        @Schema(description = "도로명 주소", example = "서울특별시 성동구 아차산로 123")
        private String roadAddress;
        @Schema(description = "상세 주소", example = "2층 201호")
        private String detailAddress;
        @Schema(description = "우편번호", example = "04782")
        private String zipCode;
        @Schema(description = "위도", example = "37.5445")
        private Double latitude;
        @Schema(description = "경도", example = "127.0560")
        private Double longitude;
        @Schema(description = "가까운 역에서 도보 시간", example = "성수역 도보 5분")
        private String nearestStationWalkTime;
        @Schema(description = "조회수", example = "1200")
        private Long viewCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ArchiverPlaceDetailPostPlace", description = "장소에 연결된 게시글 정보")
    public static class PostPlaceResponse {
        @Schema(description = "PostPlace ID", example = "00000000-0000-0000-0000-000000000401")
        private Long postPlaceId;
        @Schema(description = "게시글 ID", example = "00000000-0000-0000-0000-000000000501")
        private Long postId;
        @Schema(description = "에디터의 장소 설명", example = "분위기가 너무 좋고 커피가 맛있어요.")
        private String description;
        @Schema(description = "이미지 URL")
        private String imageUrl;
        @Schema(description = "카테고리명 목록")
        private List<String> categoryNames;
    }
}
