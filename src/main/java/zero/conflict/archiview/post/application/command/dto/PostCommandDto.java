package zero.conflict.archiview.post.application.command.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public class PostCommandDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(name = "PostCreateRequest")
    public static class Request {
        @NotBlank(message = "URL은 필수입니다.")
        @io.swagger.v3.oas.annotations.media.Schema(description = "인스타그램 게시글 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String url;

        @NotBlank(message = "해시태그는 필수입니다.")
        @io.swagger.v3.oas.annotations.media.Schema(description = "게시글 해시태그 (최대 3개)", example = "#성수카페 #감성레벨 #디저트맛집")
        private String hashTag;

        @Valid
        @NotEmpty(message = "장소 정보는 최소 1개 이상 포함되어야 합니다.")
        @jakarta.validation.constraints.Size(max = 7, message = "장소 정보는 최대 7개까지 등록 가능합니다.")
        private List<PlaceInfoRequest> placeInfoRequestList;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PlaceInfoRequest {
            @io.swagger.v3.oas.annotations.media.Schema(description = "장소명", example = "아카이브 성수")
            private String name;
            @NotBlank(message = "장소 설명은 필수입니다.")
            @io.swagger.v3.oas.annotations.media.Schema(description = "에디터의 장소 설명", example = "분위기가 너무 좋고 커피가 맛있어요.")
            private String description;
            @NotBlank(message = "도로명 주소는 필수입니다.")
            @io.swagger.v3.oas.annotations.media.Schema(description = "도로명 주소", example = "서울특별시 성동구 아차산로 123")
            private String roadAddress;
            @NotBlank
            @io.swagger.v3.oas.annotations.media.Schema(description = "상세 주소", example = "2층 201호")
            private String detailAddress;
            @NotBlank
            @io.swagger.v3.oas.annotations.media.Schema(description = "우편번호", example = "04782")
            private String zipCode;
            @NotNull(message = "위도는 필수입니다.")
            @io.swagger.v3.oas.annotations.media.Schema(description = "위도", example = "37.5445")
            private Double latitude;
            @NotNull(message = "경도는 필수입니다.")
            @io.swagger.v3.oas.annotations.media.Schema(description = "경도", example = "127.0560")
            private Double longitude;

            @io.swagger.v3.oas.annotations.media.Schema(description = "카테고리 ID 목록", example = "[\"00000000-0000-0000-0000-000000000001\", \"00000000-0000-0000-0000-000000000004\"]")
            private List<UUID> categoryIds;
            @io.swagger.v3.oas.annotations.media.Schema(description = "가까운 역에서 도보 시간", example = "성수역 도보 5분")
            private String nearestStationWalkTime;

            @io.swagger.v3.oas.annotations.media.Schema(description = "업로드된 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/posts/uuid_photo.png")
            private String imageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        @io.swagger.v3.oas.annotations.media.Schema(description = "저장된 게시글 ID", example = "00000000-0000-0000-0000-000000000001")
        private UUID postId;
        @io.swagger.v3.oas.annotations.media.Schema(description = "등록된 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String url;
        @io.swagger.v3.oas.annotations.media.Schema(description = "등록된 해시태그", example = "#성수카페 #감성레벨 #디저트맛집")
        private String hashTag;
        private List<PlaceInfoResponse> placeInfoResponseList;

        public static Response from(zero.conflict.archiview.post.domain.Post post,
                List<PlaceInfoResponse> placeInfoResponseList) {
            return Response.builder()
                    .postId(post.getId())
                    .url(post.getUrl())
                    .hashTag(post.getHashTag())
                    .placeInfoResponseList(placeInfoResponseList)
                    .build();
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PlaceInfoResponse {
            @io.swagger.v3.oas.annotations.media.Schema(description = "장소 ID", example = "00000000-0000-0000-0000-000000000101")
            private UUID placeId;
            @io.swagger.v3.oas.annotations.media.Schema(description = "장소명", example = "아카이브 성수")
            private String name;
            @io.swagger.v3.oas.annotations.media.Schema(description = "도로명 주소", example = "서울특별시 성동구 아차산로 123")
            private String roadAddress;
            @io.swagger.v3.oas.annotations.media.Schema(description = "상세 주소", example = "2층 201호")
            private String detailAddress;
            @io.swagger.v3.oas.annotations.media.Schema(description = "우편번호", example = "04782")
            private String zipCode;
            @io.swagger.v3.oas.annotations.media.Schema(description = "위도", example = "37.5445")
            private Double latitude;
            @io.swagger.v3.oas.annotations.media.Schema(description = "경도", example = "127.0560")
            private Double longitude;

            public static PlaceInfoResponse from(zero.conflict.archiview.post.domain.Place place) {
                return PlaceInfoResponse.builder()
                        .placeId(place.getId())
                        .name(place.getName())
                        .roadAddress(place.getAddress().getRoadAddress())
                        .detailAddress(place.getAddress().getDetailAddress())
                        .zipCode(place.getAddress().getZipCode())
                        .latitude(place.getPosition().getLatitude())
                        .longitude(place.getPosition().getLongitude())
                        .build();
            }

            public static PlaceInfoResponse of(UUID placeId, String name,
                    String roadAddress, String detailAddress,
                    String zipCode, Double latitude,
                    Double longitude) {
                return PlaceInfoResponse.builder()
                        .placeId(placeId)
                        .name(name)
                        .roadAddress(roadAddress)
                        .detailAddress(detailAddress)
                        .zipCode(zipCode)
                        .latitude(latitude)
                        .longitude(longitude)
                        .build();
            }

        }

    }
}
