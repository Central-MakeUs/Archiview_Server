package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

@Schema(description = "아카이버용 핫플레이스 DTO")
public class ArchiverHotPlaceDto {

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(name = "ArchiverHotPlaceListResponse", description = "아카이버 핫플레이스 목록 응답")
        public static class ListResponse {
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
                                                                        .placeId(201L)
                                                                        .name("성수 핫플 카페")
                                                                        .imageUrl("https://picsum.photos/400/300?random=21")
                                                                        .categoryNames(List.of("카페", "디저트"))
                                                                        .hashTags(List.of("#성수카페", "#감성레벨"))
                                                                        .address("서울특별시 성동구 아차산로 123")
                                                                        .viewCount(1200L)
                                                                        .build(),
                                                        PlaceCardResponse.builder()
                                                                        .placeId(202L)
                                                                        .name("연남동 맛집")
                                                                        .imageUrl("https://picsum.photos/400/300?random=22")
                                                                        .categoryNames(List.of("맛집"))
                                                                        .hashTags(List.of("#연남동", "#맛집"))
                                                                        .address("서울특별시 마포구 연남로 1")
                                                                        .viewCount(980L)
                                                                        .build()))
                                        .build();
                }
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(name = "ArchiverHotPlaceCard", description = "아카이버 핫플레이스 카드")
        public static class PlaceCardResponse {
                @Schema(description = "장소 ID", example = "00000000-0000-0000-0000-000000000201")
                private Long placeId;
                @Schema(description = "장소명", example = "성수 핫플 카페")
                private String name;
                @Schema(description = "대표 이미지 URL")
                private String imageUrl;
                @Schema(description = "카테고리명 목록")
                private List<String> categoryNames;
                @Schema(description = "게시글 해시태그 2개", example = "[\"#성수카페\", \"#감성레벨\"]")
                private List<String> hashTags;
                @Schema(description = "장소 주소", example = "서울특별시 성동구 아차산로 123")
                private String address;
                @Schema(description = "조회수", example = "1200")
                private Long viewCount;

                public static PlaceCardResponse from(
                                Place place,
                                PostPlace latestPostPlace) {
                        List<String> categories = latestPostPlace == null
                                        ? Collections.emptyList()
                                        : latestPostPlace.getPostPlaceCategories().stream()
                                                        .map(PostPlaceCategory::getCategory)
                                                        .filter(category -> category != null
                                                                        && category.getName() != null)
                                                        .map(category -> category.getName())
                                                        .toList();
                        Post post = latestPostPlace != null ? latestPostPlace.getPost() : null;
                        return PlaceCardResponse.builder()
                                        .placeId(place.getId())
                                        .name(place.getName())
                                        .imageUrl(latestPostPlace != null ? latestPostPlace.getImageUrl() : null)
                                        .categoryNames(categories)
                                        .hashTags(post != null ? post.getHashTags() : null)
                                        .address(place.getAddress() != null ? place.getAddress().getRoadAddressName()
                                                        : null)
                                        .viewCount(place.getViewCount() == null ? 0L : place.getViewCount())
                                        .build();
                }
        }
}
