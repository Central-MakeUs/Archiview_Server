package zero.conflict.archiview.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceCategory;

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
                                                        .addressName("서울특별시 성동구 성수동 123-45")
                                                        .roadAddressName("서울특별시 성동구 아차산로 123")
                                                        .latitude(37.5445)
                                                        .longitude(127.0560)
                                                        .nearestStationWalkTime("성수역 도보 5분")
                                                        .viewCount(1200L)
                                                        .saveCount(450L)
                                                        .instagramInflowCount(800L)
                                                        .directionCount(200L)
                                                        .build())
                                        .postPlaces(List.of(
                                                        PostPlaceResponse.builder()
                                                                        .postPlaceId(401L)
                                                                        .postId(501L)
                                                                        .isArchived(true)
                                                                        .editorName("아카이브 마스터")
                                                                        .editorInstagramId("archiview_master")
                                                                        .description("분위기가 너무 좋고 커피가 맛있어요.")
                                                                        .imageUrl("https://picsum.photos/400/300?random=31")
                                                                        .categoryNames(List.of("카페", "디저트"))
                                                                        .build(),
                                                        PostPlaceResponse.builder()
                                                                        .postPlaceId(402L)
                                                                        .postId(502L)
                                                                        .isArchived(false)
                                                                        .editorName("에디터A")
                                                                        .editorInstagramId("editor_a")
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
                @Schema(description = "장소 URL")
                private String placeUrl;
                @Schema(description = "전화번호")
                private String phoneNumber;
                @Schema(description = "지번 주소", example = "서울특별시 성동구 성수동 123-45")
                private String addressName;
                @Schema(description = "도로명 주소", example = "서울특별시 성동구 아차산로 123")
                private String roadAddressName;
                @Schema(description = "위도", example = "37.5445")
                private Double latitude;
                @Schema(description = "경도", example = "127.0560")
                private Double longitude;
                @Schema(description = "가까운 역에서 도보 시간", example = "성수역 도보 5분")
                private String nearestStationWalkTime;
                @Schema(description = "조회수", example = "1200")
                private Long viewCount;
                @Schema(description = "저장수", example = "450")
                private Long saveCount;
                @Schema(description = "인스타 유입수", example = "800")
                private Long instagramInflowCount;
                @Schema(description = "길찾기수", example = "200")
                private Long directionCount;

                public static PlaceResponse from(Place place, long saveCount, long instagramInflowCount,
                                long directionCount) {
                        return PlaceResponse.builder()
                                        .placeId(place.getId())
                                        .name(place.getName())
                                        .placeUrl(place.getPlaceUrl())
                                        .phoneNumber(place.getPhoneNumber())
                                        .addressName(place.getAddress() != null ? place.getAddress().getAddressName()
                                                        : null)
                                        .roadAddressName(place.getAddress() != null
                                                        ? place.getAddress().getRoadAddressName()
                                                        : null)
                                        .latitude(place.getPosition() != null ? place.getPosition().getLatitude()
                                                        : null)
                                        .longitude(place.getPosition() != null ? place.getPosition().getLongitude()
                                                        : null)
                                        .nearestStationWalkTime(place.getNearestStationWalkTime())
                                        .viewCount(place.getViewCount() == null ? 0L : place.getViewCount())
                                        .saveCount(saveCount)
                                        .instagramInflowCount(instagramInflowCount)
                                        .directionCount(directionCount)
                                        .build();
                }
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
                @Schema(description = "인스타그램 게시글 URL")
                private String instagramUrl;
                @Schema(description = "게시글 해시태그 1~3개", example = "[\"#성수카페\", \"#감성레벨\", \"#데이트코스\"]")
                private List<String> hashTags;
                @Schema(description = "에디터의 장소 설명", example = "분위기가 너무 좋고 커피가 맛있어요.")
                private String description;
                @Schema(description = "이미지 URL")
                private String imageUrl;
                @Schema(description = "카테고리명 목록")
                private List<String> categoryNames;
                @Schema(description = "에디터 이름", example = "아카이브 마스터")
                private String editorName;
                @Schema(description = "에디터 인스타그램 ID", example = "archiview_master")
                private String editorInstagramId;
                @Schema(description = "아카이버 아카이브 여부", example = "true")
                @JsonProperty("isArchived")
                @Getter(AccessLevel.NONE)
                private boolean isArchived;

                @JsonIgnore
                public boolean isArchived() {
                        return isArchived;
                }

                public static PostPlaceResponse from(
                                PostPlace postPlace,
                                String editorName,
                                String editorInstagramId,
                                boolean isArchived) {
                        Post post = postPlace.getPost();
                        List<String> categories = postPlace.getPostPlaceCategories().stream()
                                        .map(PostPlaceCategory::getCategory)
                                        .filter(category -> category != null)
                                        .map(category -> category.getName())
                                        .filter(name -> name != null)
                                        .toList();
                        return PostPlaceResponse.builder()
                                        .postPlaceId(postPlace.getId())
                                        .postId(post != null ? post.getId() : null)
                                        .instagramUrl(post != null ? post.getUrl() : null)
                                        .hashTags(post != null ? post.getHashTags() : null)
                                        .description(postPlace.getDescription())
                                        .imageUrl(postPlace.getImageUrl())
                                        .categoryNames(categories)
                                        .editorName(editorName)
                                        .editorInstagramId(editorInstagramId)
                                        .isArchived(isArchived)
                                        .build();
                }
        }
}
