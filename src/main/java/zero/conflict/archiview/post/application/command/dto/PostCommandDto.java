package zero.conflict.archiview.post.application.command.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class PostCommandDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String url;
        private String hashTag;
        private List<PlaceInfoRequest> placeInfoRequestList;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PlaceInfoRequest {
            private String name;
            private String description;
            private String roadAddress;
            private String detailAddress;
            private String zipCode;
            private BigDecimal latitude;
            private BigDecimal longitude;
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

        public static Response of(Long postId, String url, String hashTag, List<PlaceInfoResponse> placeInfoResponseList) {
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
            private BigDecimal latitude;
            private BigDecimal longitude;

            public static PlaceInfoResponse of(Long placeId, String name,
                                               String roadAddress, String detailAddress,
                                               String zipCode, BigDecimal latitude,
                                               BigDecimal longitude) {
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
