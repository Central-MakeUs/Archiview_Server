package zero.conflict.archiview.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Post;

import java.util.List;

public class PostCommandDto {

    public interface PlaceInfoInput {
        Long getPostPlaceId();

        String getPlaceName();

        String getDescription();

        String getAddressName();

        String getRoadAddressName();

        Double getLatitude();

        Double getLongitude();

        List<Long> getCategoryIds();

        String getNearestStationWalkTime();

        String getPlaceUrl();

        String getPhoneNumber();

        String getImageUrl();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PostCreateRequest")
    public static class CreateRequest {
        @NotBlank(message = "URL은 필수입니다.")
        @Schema(description = "인스타그램 게시글 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String url;

        @NotNull(message = "해시태그는 필수입니다.")
        @jakarta.validation.constraints.Size(min = 1, max = 3, message = "해시태그는 1~3개여야 합니다.")
        @Schema(description = "게시글 해시태그 1~3개", example = "[\"#성수카페\", \"#감성레벨\", \"#데이트코스\"]")
        private List<@NotBlank String> hashTags;

        @Valid
        @NotEmpty(message = "장소 정보는 최소 1개 이상 포함되어야 합니다.")
        @jakarta.validation.constraints.Size(max = 7, message = "장소 정보는 최대 7개까지 등록 가능합니다.")
        private List<CreatePlaceInfoRequest> placeInfoRequestList;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class CreatePlaceInfoRequest implements PlaceInfoInput {
            @Schema(hidden = true)
            private Long postPlaceId;

            @NotBlank(message = "장소명은 필수입니다.")
            @Schema(description = "장소명", example = "아카이브 성수")
            private String placeName;

            @NotBlank(message = "장소 설명은 필수입니다.")
            @Schema(description = "에디터의 장소 설명", example = "분위기가 너무 좋고 커피가 맛있어요.")
            private String description;
            @NotBlank(message = "지번 주소는 필수입니다.")
            @Schema(description = "지번 주소", example = "서울 노원구 공릉동 596-12")
            private String addressName;
            @NotBlank(message = "도로명 주소는 필수입니다.")
            @Schema(description = "도로명 주소", example = "인천 중구 백운로228번길 81-10")
            private String roadAddressName;
            @NotNull(message = "위도는 필수입니다.")
            @Schema(description = "위도", example = "37.5445")
            private Double latitude;
            @NotNull(message = "경도는 필수입니다.")
            @Schema(description = "경도", example = "127.0560")
            private Double longitude;

            @Schema(description = "카테고리 ID 목록", example = "[1, 2]")
            private List<Long> categoryIds;
            @Schema(description = "가까운 역에서 도보 시간", example = "성수역 도보 5분")
            private String nearestStationWalkTime;

            @Schema(description = "장소 URL", example = "https://place.map.kakao.com/123456")
            private String placeUrl;

            @Schema(description = "전화번호", example = "02-1234-5678")
            private String phoneNumber;

            @jakarta.validation.constraints.Size(max = 1000, message = "이미지 URL은 최대 1000자여야 합니다.")
            @Schema(description = "업로드된 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/posts/uuid_photo.png")
            private String imageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PostUpdateRequest")
    public static class UpdateRequest {
        @NotBlank(message = "URL은 필수입니다.")
        @Schema(description = "인스타그램 게시글 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String url;

        @NotNull(message = "해시태그는 필수입니다.")
        @jakarta.validation.constraints.Size(min = 1, max = 3, message = "해시태그는 1~3개여야 합니다.")
        @Schema(description = "게시글 해시태그 1~3개", example = "[\"#성수카페\", \"#감성레벨\", \"#데이트코스\"]")
        private List<@NotBlank String> hashTags;

        @Valid
        @NotEmpty(message = "장소 정보는 최소 1개 이상 포함되어야 합니다.")
        @jakarta.validation.constraints.Size(max = 7, message = "장소 정보는 최대 7개까지 등록 가능합니다.")
        private List<UpdatePlaceInfoRequest> placeInfoRequestList;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class UpdatePlaceInfoRequest implements PlaceInfoInput {
            @Schema(description = "수정 시 기존 postPlace ID (신규 추가는 null)", example = "101")
            private Long postPlaceId;

            @NotBlank(message = "장소명은 필수입니다.")
            @Schema(description = "장소명", example = "아카이브 성수")
            private String placeName;

            @NotBlank(message = "장소 설명은 필수입니다.")
            @Schema(description = "에디터의 장소 설명", example = "분위기가 너무 좋고 커피가 맛있어요.")
            private String description;
            @NotBlank(message = "지번 주소는 필수입니다.")
            @Schema(description = "지번 주소", example = "서울 노원구 공릉동 596-12")
            private String addressName;
            @NotBlank(message = "도로명 주소는 필수입니다.")
            @Schema(description = "도로명 주소", example = "인천 중구 백운로228번길 81-10")
            private String roadAddressName;
            @NotNull(message = "위도는 필수입니다.")
            @Schema(description = "위도", example = "37.5445")
            private Double latitude;
            @NotNull(message = "경도는 필수입니다.")
            @Schema(description = "경도", example = "127.0560")
            private Double longitude;

            @Schema(description = "카테고리 ID 목록", example = "[1, 2]")
            private List<Long> categoryIds;
            @Schema(description = "가까운 역에서 도보 시간", example = "성수역 도보 5분")
            private String nearestStationWalkTime;

            @Schema(description = "장소 URL", example = "https://place.map.kakao.com/123456")
            private String placeUrl;

            @Schema(description = "전화번호", example = "02-1234-5678")
            private String phoneNumber;

            @jakarta.validation.constraints.Size(max = 1000, message = "이미지 URL은 최대 1000자여야 합니다.")
            @Schema(description = "업로드된 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/posts/uuid_photo.png")
            private String imageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PostCommandResponse")
    public static class Response {
        @Schema(description = "저장된 게시글 ID", example = "00000000-0000-0000-0000-000000000001")
        private Long postId;
        @Schema(description = "등록된 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String url;
        @Schema(description = "등록된 해시태그 1~3개", example = "[\"#성수카페\", \"#감성레벨\", \"#데이트코스\"]")
        private List<String> hashTags;
        private List<PlaceInfoResponse> placeInfoResponseList;

        public static Response from(Post post,
                List<PlaceInfoResponse> placeInfoResponseList) {
            return Response.builder()
                    .postId(post.getId())
                    .url(post.getUrl())
                    .hashTags(post.getHashTags())
                    .placeInfoResponseList(placeInfoResponseList)
                    .build();
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PlaceInfoResponse {
            @Schema(description = "장소 ID", example = "00000000-0000-0000-0000-000000000101")
            private Long placeId;
            @Schema(description = "장소명", example = "아카이브 성수")
            private String placeName;
            @Schema(description = "장소명(하위호환)", example = "아카이브 성수")
            private String name;
            @Schema(description = "지번 주소", example = "서울 노원구 공릉동 596-12")
            private String addressName;
            @Schema(description = "도로명 주소", example = "인천 중구 백운로228번길 81-10")
            private String roadAddressName;
            @Schema(description = "위도", example = "37.5445")
            private Double latitude;
            @Schema(description = "경도", example = "127.0560")
            private Double longitude;
            @Schema(description = "장소 URL", example = "https://place.map.kakao.com/123456")
            private String placeUrl;
            @Schema(description = "전화번호", example = "02-1234-5678")
            private String phoneNumber;

            public static PlaceInfoResponse from(Place place) {
                return PlaceInfoResponse.builder()
                        .placeId(place.getId())
                        .placeName(place.getName())
                        .name(place.getName())
                        .addressName(place.getAddress().getAddressName())
                        .roadAddressName(place.getAddress().getRoadAddressName())
                        .latitude(place.getPosition().getLatitude())
                        .longitude(place.getPosition().getLongitude())
                        .placeUrl(place.getPlaceUrl())
                        .phoneNumber(place.getPhoneNumber())
                        .build();
            }

            public static PlaceInfoResponse of(Long placeId, String name,
                    String addressName, String roadAddressName,
                    Double latitude,
                    Double longitude) {
                return PlaceInfoResponse.builder()
                        .placeId(placeId)
                        .placeName(name)
                        .name(name)
                        .addressName(addressName)
                        .roadAddressName(roadAddressName)
                        .latitude(latitude)
                        .longitude(longitude)
                        .build();
            }

        }

    }
}
