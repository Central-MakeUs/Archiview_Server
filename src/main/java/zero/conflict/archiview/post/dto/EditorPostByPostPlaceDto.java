package zero.conflict.archiview.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceCategory;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "postPlaceId 기준 게시글 상세 조회 DTO")
public class EditorPostByPostPlaceDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "EditorPostByPostPlaceResponse")
    public static class Response {
        @Schema(description = "게시글 ID")
        private Long postId;
        @Schema(description = "인스타그램 게시글 URL")
        private String url;
        @Schema(description = "해시태그 1~3개")
        private List<String> hashTags;
        @Schema(description = "게시글에 포함된 장소 목록")
        private List<PostPlaceResponse> postPlaces;

        public static Response from(Post post, List<PostPlace> postPlaces) {
            return Response.builder()
                    .postId(post.getId())
                    .url(post.getUrl())
                    .hashTags(post.getHashTags())
                    .postPlaces(postPlaces.stream()
                            .map(PostPlaceResponse::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "EditorPostByPostPlacePostPlaceResponse")
    public static class PostPlaceResponse {
        @Schema(description = "PostPlace ID")
        private Long postPlaceId;
        @Schema(description = "에디터 설명")
        private String description;
        @Schema(description = "이미지 URL")
        private String imageUrl;
        @Schema(description = "조회수")
        private Long viewCount;
        @Schema(description = "저장수")
        private Long saveCount;
        @Schema(description = "인스타 유입수")
        private Long instagramInflowCount;
        @Schema(description = "길찾기수")
        private Long directionCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "장소카드 생성 시각")
        private LocalDateTime postPlaceCreatedAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "장소카드 최종 수정 시각")
        private LocalDateTime postPlaceLastModifiedAt;

        @Schema(description = "장소 ID")
        private Long placeId;
        @Schema(description = "장소명")
        private String placeName;
        @Schema(description = "장소 URL")
        private String placeUrl;
        @Schema(description = "전화번호")
        private String phoneNumber;
        @Schema(description = "지번 주소")
        private String addressName;
        @Schema(description = "도로명 주소")
        private String roadAddressName;
        @Schema(description = "위도")
        private Double latitude;
        @Schema(description = "경도")
        private Double longitude;
        @Schema(description = "가까운 역 도보 시간")
        private String nearestStationWalkTime;
        @Schema(description = "장소 조회수")
        private Long placeViewCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "장소 생성 시각")
        private LocalDateTime placeCreatedAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "장소 최종 수정 시각")
        private LocalDateTime placeLastModifiedAt;

        @Schema(description = "카테고리 ID 목록")
        private List<Long> categoryIds;
        @Schema(description = "카테고리명 목록")
        private List<String> categoryNames;

        public static PostPlaceResponse from(PostPlace postPlace) {
            Place place = postPlace.getPlace();
            List<Category> categories = postPlace.getPostPlaceCategories().stream()
                    .map(PostPlaceCategory::getCategory)
                    .filter(category -> category != null)
                    .toList();

            return PostPlaceResponse.builder()
                    .postPlaceId(postPlace.getId())
                    .description(postPlace.getDescription())
                    .imageUrl(postPlace.getImageUrl())
                    .viewCount(defaultZero(postPlace.getViewCount()))
                    .saveCount(defaultZero(postPlace.getSaveCount()))
                    .instagramInflowCount(defaultZero(postPlace.getInstagramInflowCount()))
                    .directionCount(defaultZero(postPlace.getDirectionCount()))
                    .postPlaceCreatedAt(postPlace.getCreatedAt())
                    .postPlaceLastModifiedAt(postPlace.getLastModifiedAt())
                    .placeId(place != null ? place.getId() : null)
                    .placeName(place != null ? place.getName() : null)
                    .placeUrl(place != null ? place.getPlaceUrl() : null)
                    .phoneNumber(place != null ? place.getPhoneNumber() : null)
                    .addressName(place != null && place.getAddress() != null ? place.getAddress().getAddressName() : null)
                    .roadAddressName(
                            place != null && place.getAddress() != null ? place.getAddress().getRoadAddressName() : null)
                    .latitude(place != null && place.getPosition() != null ? place.getPosition().getLatitude() : null)
                    .longitude(place != null && place.getPosition() != null ? place.getPosition().getLongitude() : null)
                    .nearestStationWalkTime(place != null ? place.getNearestStationWalkTime() : null)
                    .placeViewCount(place != null ? defaultZero(place.getViewCount()) : 0L)
                    .placeCreatedAt(place != null ? place.getCreatedAt() : null)
                    .placeLastModifiedAt(place != null ? place.getLastModifiedAt() : null)
                    .categoryIds(categories.stream().map(Category::getId).toList())
                    .categoryNames(categories.stream().map(Category::getName).toList())
                    .build();
        }

        private static long defaultZero(Long value) {
            return value == null ? 0L : value;
        }
    }
}
