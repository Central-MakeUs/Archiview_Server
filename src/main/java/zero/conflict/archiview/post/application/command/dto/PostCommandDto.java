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

public class PostCommandDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "URL은 필수입니다.")
        private String url;
        @NotBlank(message = "해시태그는 필수입니다.")
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
            private String name;
            @NotBlank(message = "장소 설명은 필수입니다.")
            private String description;
            @NotBlank(message = "도로명 주소는 필수입니다.")
            private String roadAddress;
            @NotBlank
            private String detailAddress;
            @NotBlank
            private String zipCode;
            @NotNull(message = "위도는 필수입니다.")
            private Double latitude;
            @NotNull(message = "경도는 필수입니다.")
            private Double longitude;

            private List<Long> categoryIds;
            private String nearestStationWalkTime;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long postId;
        private String url;
        private String hashTag;
        private List<PlaceInfoResponse> placeInfoResponseList;

        public static Response of(Long postId, String url, String hashTag,
                List<PlaceInfoResponse> placeInfoResponseList) {
            return Response.builder()
                    .postId(postId)
                    .url(url)
                    .hashTag(hashTag)
                    .placeInfoResponseList(placeInfoResponseList)
                    .build();
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PlaceInfoResponse {
            private Long placeId;
            private String name;
            private String roadAddress;
            private String detailAddress;
            private String zipCode;
            private Double latitude;
            private Double longitude;

            public static PlaceInfoResponse of(Long placeId, String name,
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
