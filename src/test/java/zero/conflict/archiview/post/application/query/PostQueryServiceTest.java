package zero.conflict.archiview.post.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.archiver.query.ArchiverVisibilityService;
import zero.conflict.archiview.post.application.editor.query.EditorPostQueryService;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceArchiveRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.post.application.archiver.query.PostQueryService;
import zero.conflict.archiview.post.domain.*;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverArchivedPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;
import zero.conflict.archiview.post.infrastructure.persistence.CategoryPlaceReadRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostQueryService 테스트")
class PostQueryServiceTest {

        @InjectMocks
        private PostQueryService postQueryService;

        @InjectMocks
        private EditorPostQueryService editorPostQueryService;

        @Mock
        private PostPlaceRepository postPlaceRepository;

        @Mock
        private PlaceRepository placeRepository;

        @Mock
        private PostRepository postRepository;

        @Mock
        private PostPlaceArchiveRepository postPlaceSaveRepository;

        @Mock
        private UserClient userClient;

        @Mock
        private CategoryPlaceReadRepository categoryPlaceReadRepository;

        @Mock
        private ArchiverVisibilityService archiverVisibilityService;

        @Test
        @DisplayName("좌표 기준 1km 내 장소 목록을 조회한다")
        void getNearbyPlacesWithin1km_success() {
                // given
                Double latitude = 37.5445;
                Double longitude = 127.0560;
                Place place = Place.builder().id(11L).name("성수 카페").viewCount(100L).build();
                Post post = Post.builder().id(10L).build();
                PostPlace postPlace = PostPlace.builder()
                                .id(21L)
                                .post(post)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("최근 설명")
                                .viewCount(100L)
                                .saveCount(20L)
                                .build();
                setField(postPlace, "createdAt", LocalDateTime.of(2026, 2, 10, 12, 0, 0));

                given(categoryPlaceReadRepository.findPlacesNearby(latitude, longitude, 1000))
                                .willReturn(List.of(place));
                given(postPlaceRepository.findAllByPlaceIds(List.of(11L))).willReturn(List.of(postPlace));

                // when
                CategoryQueryDto.CategoryPlaceListResponse response = postQueryService
                                .getNearbyPlacesWithin1km(latitude, longitude);

                // then
                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getTotalCount()).isEqualTo(1L);
                assertThat(response.getPlaces().get(0).getPlaceId()).isEqualTo(11L);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("성수 카페");
                assertThat(response.getPlaces().get(0).getLatestDescription()).isEqualTo("최근 설명");
                assertThat(response.getPlaces().get(0).getViewCount()).isEqualTo(100L);
                assertThat(response.getPlaces().get(0).getSaveCount()).isEqualTo(20L);
        }

        @Test
        @DisplayName("좌표 기준 1km 내 장소 목록 조회(아카이버) - 가시성 필터를 적용한다")
        void getNearbyPlacesWithin1km_withArchiver_filtersInvisible() {
                Double latitude = 37.5445;
                Double longitude = 127.0560;
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();

                Place visiblePlace = Place.builder().id(11L).name("노출 장소").viewCount(100L).build();
                Place hiddenPlace = Place.builder().id(12L).name("숨김 장소").viewCount(80L).build();
                Post post = Post.builder().id(10L).build();

                PostPlace visiblePostPlace = PostPlace.builder()
                                .id(21L)
                                .post(post)
                                .place(visiblePlace)
                                .editorId(editorId)
                                .description("노출 설명")
                                .saveCount(20L)
                                .build();
                PostPlace hiddenPostPlace = PostPlace.builder()
                                .id(22L)
                                .post(post)
                                .place(hiddenPlace)
                                .editorId(editorId)
                                .description("숨김 설명")
                                .saveCount(10L)
                                .build();
                setField(visiblePostPlace, "createdAt", LocalDateTime.of(2026, 2, 10, 12, 0, 0));
                setField(hiddenPostPlace, "createdAt", LocalDateTime.of(2026, 2, 10, 11, 0, 0));

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = new ArchiverVisibilityService.VisibilityFilter(
                                java.util.Set.of(),
                                java.util.Set.of());

                given(categoryPlaceReadRepository.findPlacesNearby(latitude, longitude, 1000))
                                .willReturn(List.of(visiblePlace, hiddenPlace));
                given(postPlaceRepository.findAllByPlaceIds(List.of(11L, 12L)))
                                .willReturn(List.of(visiblePostPlace, hiddenPostPlace));
                given(archiverVisibilityService.getVisibilityFilter(archiverId)).willReturn(visibilityFilter);
                given(archiverVisibilityService.filterVisiblePostPlaces(
                                org.mockito.ArgumentMatchers.eq(List.of(visiblePostPlace, hiddenPostPlace)),
                                org.mockito.ArgumentMatchers.eq(visibilityFilter)))
                                .willReturn(List.of(visiblePostPlace));

                CategoryQueryDto.CategoryPlaceListResponse response = postQueryService
                                .getNearbyPlacesWithin1km(latitude, longitude, archiverId);

                assertThat(response.getTotalCount()).isEqualTo(1L);
                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getPlaceId()).isEqualTo(11L);
                assertThat(response.getPlaces().get(0).getLatestDescription()).isEqualTo("노출 설명");
        }

        @Test
        @DisplayName("postPlaceId로 게시글과 게시글 내 장소 목록을 조회한다")
        void getPostByPostPlaceId_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Post post = Post.createOf(editorId, "https://www.instagram.com/p/test-post", List.of("#하나"));
                Place place1 = Place.createOf("장소1", Address.of("주소1", "도로1"), Position.of(37.1, 127.1), "도보 3분",
                                "https://place1.url", "02-1111-1111");
                Place place2 = Place.createOf("장소2", Address.of("주소2", "도로2"), Position.of(37.2, 127.2), "도보 5분",
                                "https://place2.url", "02-2222-2222");

                PostPlace postPlace1 = PostPlace.createOf(post, place1, "설명1", "https://img1.url", editorId);
                PostPlace postPlace2 = PostPlace.createOf(post, place2, "설명2", "https://img2.url", editorId);

                setField(post, "id", 10L);
                setField(postPlace1, "id", 100L);
                setField(postPlace2, "id", 101L);

                given(postPlaceRepository.findById(100L)).willReturn(java.util.Optional.of(postPlace1));
                given(postPlaceRepository.findAllByPostId(10L)).willReturn(List.of(postPlace1, postPlace2));

                // when
                EditorPostByPostPlaceDto.Response response = editorPostQueryService.getPostByPostPlaceId(100L);

                // then
                assertThat(response.getPostId()).isEqualTo(10L);
                assertThat(response.getPostPlaces()).hasSize(2);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(100L);
                assertThat(response.getPostPlaces().get(1).getPostPlaceId()).isEqualTo(101L);
                assertThat(response.getPostPlaces().get(0).getPlaceUrl()).isEqualTo("https://place1.url");
                assertThat(response.getPostPlaces().get(0).getPhoneNumber()).isEqualTo("02-1111-1111");
        }

        @Test
        @DisplayName("에디터의 모든 핀을 조회한다")
        void getMapPins_all_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Category category = Category.builder().id(1L).name("한식").build();

                Place place = Place.builder()
                                .id(1L)
                                .name("식당")
                                .position(Position.of(37.0, 127.0))
                                .build();

                Post post = Post.builder().id(1L).build();

                PostPlace postPlace = PostPlace.builder()
                                .id(1L)
                                .post(post)
                                .place(place)
                                .editorId(editorId)
                                .build();
                postPlace.addCategory(category);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(postPlace));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place));

                // when
                EditorMapDto.Response response = editorPostQueryService.getMapPins(
                                editorId,
                                MapFilter.ALL,
                                null,
                                null,
                                null);

                // then
                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getName()).isEqualTo("식당");
                assertThat(response.getPins().get(0).getCategoryIds()).containsExactly(1L);
        }

        @Test
        @DisplayName("카테고리로 핀을 필터링한다")
        void getMapPins_withCategoryFilter() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Category category1 = Category.builder().id(1L).name("한식").build();
                Category category2 = Category.builder().id(2L).name("양식").build();

                Place place1 = Place.builder().id(1L).name("한식당").position(Position.of(37.0, 127.0)).build();
                Place place2 = Place.builder().id(2L).name("양식당").position(Position.of(37.1, 127.1)).build();

                Post post = Post.builder().id(1L).build();

                PostPlace pp1 = PostPlace.builder().id(1L).post(post).place(place1).editorId(editorId).build();
                pp1.addCategory(category1);

                PostPlace pp2 = PostPlace.builder().id(2L).post(post).place(place2).editorId(editorId).build();
                pp2.addCategory(category2);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place1));

                // when (Filter by category1)
                EditorMapDto.Response response = editorPostQueryService.getMapPins(
                                editorId,
                                MapFilter.ALL,
                                List.of(category1.getId()),
                                null,
                                null);

                // then
                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getName()).isEqualTo("한식당");
        }

        @Test
        @DisplayName("내 주변 핀만 조회한다")
        void getMapPins_nearbyFilter() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Place nearPlace = Place.builder().id(1L).name("가까운곳").position(Position.of(37.0001, 127.0001)).build();
                Place farPlace = Place.builder().id(2L).name("먼곳").position(Position.of(37.5, 127.5)).build();

                Post post = Post.builder().id(1L).build();

                PostPlace pp1 = PostPlace.builder().id(1L).post(post).place(nearPlace).editorId(editorId).build();
                PostPlace pp2 = PostPlace.builder().id(2L).post(post).place(farPlace).editorId(editorId).build();

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(nearPlace, farPlace));

                // when
                EditorMapDto.Response response = editorPostQueryService.getMapPins(
                                editorId,
                                MapFilter.NEARBY,
                                null,
                                37.0,
                                127.0);

                // then
                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getName()).isEqualTo("가까운곳");
        }

        @Test
        @DisplayName("내 주변 핀 조회 시 좌표가 없으면 예외가 발생한다")
        void getMapPins_nearbyWithoutCoordinates_throwsException() {
                UUID editorId = UUID.randomUUID();

                assertThatThrownBy(() -> editorPostQueryService.getMapPins(
                                editorId,
                                MapFilter.NEARBY,
                                null,
                                null,
                                127.0))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.INVALID_POSITION_LATITUDE);
        }

        @Test
        @DisplayName("좌표가 null인 장소는 지도 핀에서 제외한다")
        void getMapPins_excludesPlaceWithNullCoordinates() {
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();

                Place validPlace = Place.builder().id(1L).name("정상 장소").position(Position.of(37.0, 127.0)).build();
                Position invalidPosition = Position.of(37.1, 127.1);
                setField(invalidPosition, "latitude", null);
                Place invalidPlace = Place.builder().id(2L).name("비정상 장소").position(invalidPosition).build();

                PostPlace validPostPlace = PostPlace.builder().id(1L).post(post).place(validPlace).editorId(editorId).build();
                PostPlace invalidPostPlace = PostPlace.builder().id(2L).post(post).place(invalidPlace).editorId(editorId).build();

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(validPostPlace, invalidPostPlace));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(validPlace, invalidPlace));

                EditorMapDto.Response response = editorPostQueryService.getMapPins(
                                editorId,
                                MapFilter.ALL,
                                null,
                                null,
                                null);

                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getName()).isEqualTo("정상 장소");
        }

        @Test
        @DisplayName("업로드 장소 목록에 카테고리 필터를 적용한다")
        void getUploadedPlaces_withCategoryFilter() {
                UUID editorId = UUID.randomUUID();
                Category category1 = Category.builder().id(1L).name("한식").build();
                Category category2 = Category.builder().id(2L).name("양식").build();

                Place place1 = Place.builder().id(1L).name("한식당").position(Position.of(37.0, 127.0)).build();
                Place place2 = Place.builder().id(2L).name("양식당").position(Position.of(37.1, 127.1)).build();
                Post post = Post.builder().id(1L).build();

                PostPlace pp1 = PostPlace.builder().id(1L).post(post).place(place1).editorId(editorId).build();
                PostPlace pp2 = PostPlace.builder().id(2L).post(post).place(place2).editorId(editorId).build();
                pp1.addCategory(category1);
                pp2.addCategory(category2);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place1, place2));

                EditorUploadedPlaceDto.ListResponse response = editorPostQueryService.getUploadedPlaces(
                                editorId,
                                MapFilter.ALL,
                                EditorUploadedPlaceDto.PlaceSort.UPDATED,
                                List.of(category1.getId()),
                                null,
                                null);

                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("한식당");
        }

        @Test
        @DisplayName("업로드 장소 목록에 내 주변 필터를 적용한다")
        void getUploadedPlaces_nearbyFilter() {
                UUID editorId = UUID.randomUUID();
                Place nearPlace = Place.builder().id(1L).name("가까운곳").position(Position.of(37.0001, 127.0001)).build();
                Place farPlace = Place.builder().id(2L).name("먼곳").position(Position.of(37.5, 127.5)).build();
                Post post = Post.builder().id(1L).build();

                PostPlace pp1 = PostPlace.builder().id(1L).post(post).place(nearPlace).editorId(editorId).build();
                PostPlace pp2 = PostPlace.builder().id(2L).post(post).place(farPlace).editorId(editorId).build();

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(nearPlace, farPlace));

                EditorUploadedPlaceDto.ListResponse response = editorPostQueryService.getUploadedPlaces(
                                editorId,
                                MapFilter.NEARBY,
                                EditorUploadedPlaceDto.PlaceSort.UPDATED,
                                null,
                                37.0,
                                127.0);

                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("가까운곳");
        }

        @Test
        @DisplayName("업로드 장소 목록의 NEARBY 필터에서 좌표가 null인 장소는 제외한다")
        void getUploadedPlaces_nearbyFilter_excludesNullCoordinates() {
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();

                Place nearPlace = Place.builder().id(1L).name("가까운곳").position(Position.of(37.0001, 127.0001)).build();
                Position invalidPosition = Position.of(37.0, 127.0);
                setField(invalidPosition, "longitude", null);
                Place invalidPlace = Place.builder().id(2L).name("좌표오류").position(invalidPosition).build();

                PostPlace nearPostPlace = PostPlace.builder().id(1L).post(post).place(nearPlace).editorId(editorId).build();
                PostPlace invalidPostPlace = PostPlace.builder().id(2L).post(post).place(invalidPlace).editorId(editorId).build();

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(nearPostPlace, invalidPostPlace));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(nearPlace, invalidPlace));

                EditorUploadedPlaceDto.ListResponse response = editorPostQueryService.getUploadedPlaces(
                                editorId,
                                MapFilter.NEARBY,
                                EditorUploadedPlaceDto.PlaceSort.UPDATED,
                                null,
                                37.0,
                                127.0);

                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("가까운곳");
        }

        @Test
        @DisplayName("업로드 장소 목록을 업데이트순으로 정렬한다")
        void getUploadedPlaces_sortedByUpdatedDesc() {
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();

                Place place1 = Place.builder().id(1L).name("업데이트최신").position(Position.of(37.0, 127.0)).build();
                Place place2 = Place.builder().id(2L).name("업데이트과거").position(Position.of(37.1, 127.1)).build();

                PostPlace pp1 = PostPlace.builder().id(11L).post(post).place(place1).editorId(editorId).build();
                setField(pp1, "createdAt", LocalDateTime.of(2026, 2, 1, 10, 0));
                setField(pp1, "lastModifiedAt", LocalDateTime.of(2026, 2, 20, 9, 0));

                PostPlace pp2 = PostPlace.builder().id(12L).post(post).place(place2).editorId(editorId).build();
                setField(pp2, "createdAt", LocalDateTime.of(2026, 2, 10, 10, 0));
                setField(pp2, "lastModifiedAt", LocalDateTime.of(2026, 2, 15, 9, 0));

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place1, place2));

                EditorUploadedPlaceDto.ListResponse response = editorPostQueryService.getUploadedPlaces(
                                editorId,
                                MapFilter.ALL,
                                EditorUploadedPlaceDto.PlaceSort.UPDATED,
                                null,
                                null,
                                null);

                assertThat(response.getPlaces()).hasSize(2);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("업데이트최신");
                assertThat(response.getPlaces().get(1).getPlaceName()).isEqualTo("업데이트과거");
        }

        @Test
        @DisplayName("업로드 장소 목록을 등록순으로 정렬한다")
        void getUploadedPlaces_sortedByCreatedDesc() {
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();

                Place place1 = Place.builder().id(1L).name("등록과거").position(Position.of(37.0, 127.0)).build();
                Place place2 = Place.builder().id(2L).name("등록최신").position(Position.of(37.1, 127.1)).build();

                PostPlace pp1 = PostPlace.builder().id(11L).post(post).place(place1).editorId(editorId).build();
                setField(pp1, "createdAt", LocalDateTime.of(2026, 2, 1, 10, 0));
                setField(pp1, "lastModifiedAt", LocalDateTime.of(2026, 2, 20, 9, 0));

                PostPlace pp2 = PostPlace.builder().id(12L).post(post).place(place2).editorId(editorId).build();
                setField(pp2, "createdAt", LocalDateTime.of(2026, 2, 10, 10, 0));
                setField(pp2, "lastModifiedAt", LocalDateTime.of(2026, 2, 15, 9, 0));

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place1, place2));

                EditorUploadedPlaceDto.ListResponse response = editorPostQueryService.getUploadedPlaces(
                                editorId,
                                MapFilter.ALL,
                                EditorUploadedPlaceDto.PlaceSort.CREATED,
                                null,
                                null,
                                null);

                assertThat(response.getPlaces()).hasSize(2);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("등록최신");
                assertThat(response.getPlaces().get(1).getPlaceName()).isEqualTo("등록과거");
        }

        @Test
        @DisplayName("에디터의 특정 장소 인사이트 상세를 조회한다")
        void getInsightPlaceDetail_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Long placeId = 1L;

                zero.conflict.archiview.user.domain.EditorProfile editorProfile = zero.conflict.archiview.user.domain.EditorProfile
                                .builder()
                                .user(zero.conflict.archiview.user.domain.User.builder().id(editorId).build())
                                .introduction("소개")
                                .instagramUrl("https://www.instagram.com/editor_insta")
                                .nickname("에디터")
                                .instagramId("editor_insta")
                                .build();

                Place place = Place.builder()
                                .id(placeId)
                                .name("인사이트 장소")
                                .phoneNumber("02-1234-5678")
                                .address(Address.of("서울 성동구 성수동 1-1", "서울 성동구 아차산로 1"))
                                .nearestStationWalkTime("성수역 도보 3분")
                                .build();

                Post post = Post.createOf(editorId, "https://www.instagram.com/post", List.of("#성수카페", "#감성"));

                PostPlace postPlace = PostPlace.createOf(post, place, "설명", "https://url.com", editorId);
                try {
                        java.lang.reflect.Field idField = PostPlace.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(postPlace, 100L);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                }
                Category category = Category.createOf("카페");
                try {
                        java.lang.reflect.Field catIdField = Category.class.getDeclaredField("id");
                        catIdField.setAccessible(true);
                        catIdField.set(category, 1L);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                }
                postPlace.addCategory(category);
                setField(postPlace, "viewCount", 11L);
                setField(postPlace, "saveCount", 7L);
                setField(postPlace, "instagramInflowCount", 5L);
                setField(postPlace, "directionCount", 3L);

                given(postPlaceRepository.findAllByEditorIdAndPlaceId(editorId, placeId))
                                .willReturn(List.of(postPlace));
                given(placeRepository.findById(placeId)).willReturn(java.util.Optional.of(place));
                given(userClient.getEditorSummaries(List.of(editorId)))
                                .willReturn(java.util.Map.of(
                                                editorId,
                                                new UserClient.EditorSummary(
                                                                editorId,
                                                                editorProfile.getNickname(),
                                                                editorProfile.getInstagramId())));

                // when
                zero.conflict.archiview.post.dto.EditorInsightDto.PlaceDetailResponse response = editorPostQueryService
                                .getInsightPlaceDetail(editorId, placeId);

                // then
                assertThat(response.getPostPlaces()).hasSize(1);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(100L);
                assertThat(response.getPostPlaces().get(0).getEditorName()).isEqualTo("에디터");
                assertThat(response.getPostPlaces().get(0).getImageUrl()).isEqualTo("https://url.com");
                assertThat(response.getPlaceName()).isEqualTo("인사이트 장소");
                assertThat(response.getPhoneNumber()).isEqualTo("02-1234-5678");
                assertThat(response.getPlaceImageUrl()).isEqualTo("https://url.com");
                assertThat(response.getEditorTotal()).isEqualTo(1L);
                assertThat(response.getAddress().getAddressName()).isEqualTo("서울 성동구 성수동 1-1");
                assertThat(response.getNearestStationWalkTime()).isEqualTo("성수역 도보 3분");
                assertThat(response.getStats().getViewCount()).isEqualTo(11L);
                assertThat(response.getStats().getSaveCount()).isEqualTo(7L);
                assertThat(response.getStats().getInstagramInflowCount()).isEqualTo(5L);
                assertThat(response.getStats().getDirectionCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("에디터 인사이트 상세 조회 시 장소 정보가 없으면 예외")
        void getInsightPlaceDetail_placeNotFound_throwsException() {
                UUID editorId = UUID.randomUUID();
                Long placeId = 1L;

                Place anotherPlace = Place.builder().id(99L).name("다른 장소").build();
                Post post = Post.createOf(editorId, "https://www.instagram.com/post", List.of("#성수카페"));
                PostPlace postPlace = PostPlace.createOf(post, anotherPlace, "설명", "https://url.com", editorId);

                given(postPlaceRepository.findAllByEditorIdAndPlaceId(editorId, placeId))
                                .willReturn(List.of(postPlace));
                given(placeRepository.findById(placeId)).willReturn(java.util.Optional.empty());
                given(userClient.getEditorSummaries(List.of(editorId)))
                                .willReturn(java.util.Map.of(
                                                editorId,
                                                new UserClient.EditorSummary(editorId, "에디터", "editor_insta")));

                assertThatThrownBy(() -> editorPostQueryService.getInsightPlaceDetail(editorId, placeId))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.POST_PLACE_NOT_FOUND);
        }

        @Test
        @DisplayName("아카이버용 특정 장소 상세를 조회한다")
        void getArchiverPlaceDetail_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Long placeId = 1L;

                zero.conflict.archiview.user.domain.EditorProfile editorProfile = zero.conflict.archiview.user.domain.EditorProfile
                                .builder()
                                .user(zero.conflict.archiview.user.domain.User.builder().id(editorId).build())
                                .introduction("소개")
                                .instagramUrl("https://www.instagram.com/editor_insta")
                                .nickname("에디터")
                                .instagramId("editor_insta")
                                .build();

                Place place = Place.builder()
                                .id(placeId)
                                .name("성수 핫플")
                                .phoneNumber("02-1234-5678")
                                .build();

                Post post = Post.createOf(editorId, "https://www.instagram.com/post", List.of("#성수카페", "#감성"));

                PostPlace postPlace = PostPlace.createOf(post, place, "설명", "https://url.com", editorId);
                try {
                        java.lang.reflect.Field idField = PostPlace.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(postPlace, 100L);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                }

                given(placeRepository.findById(placeId)).willReturn(java.util.Optional.of(place));
                given(postPlaceRepository.findAllByPlaceId(placeId)).willReturn(List.of(postPlace));
                given(userClient.getEditorSummaries(List.of(editorId)))
                                .willReturn(java.util.Map.of(
                                                editorId,
                                                new UserClient.EditorSummary(
                                                                editorId,
                                                                editorProfile.getNickname(),
                                                                editorProfile.getInstagramId())));

                // when
                zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto.Response response = postQueryService
                                .getArchiverPlaceDetail(placeId);

                // then
                assertThat(response.getPlace().getPlaceId()).isEqualTo(placeId);
                assertThat(response.getPlace().getPhoneNumber()).isEqualTo("02-1234-5678");
                assertThat(response.getPostPlaces()).hasSize(1);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(100L);
                assertThat(response.getPostPlaces().get(0).getEditorName()).isEqualTo("에디터");
                assertThat(response.getPostPlaces().get(0).getEditorInstagramId()).isEqualTo("editor_insta");
                assertThat(response.getPostPlaces().get(0).isArchived()).isFalse();
        }

        @Test
        @DisplayName("아카이버용 특정 장소 상세 조회 시 postPlace별 아카이브 여부를 반환한다")
        void getArchiverPlaceDetail_withArchiveFlag_success() {
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();
                Long placeId = 1L;

                Place place = Place.builder()
                                .id(placeId)
                                .name("성수 핫플")
                                .build();
                Post post = Post.createOf(editorId, "https://www.instagram.com/post", List.of("#성수카페", "#감성"));
                PostPlace archivedPostPlace = PostPlace.createOf(post, place, "설명1", "https://url1.com", editorId);
                PostPlace nonArchivedPostPlace = PostPlace.createOf(post, place, "설명2", "https://url2.com", editorId);
                setField(archivedPostPlace, "id", 100L);
                setField(nonArchivedPostPlace, "id", 101L);

                given(placeRepository.findById(placeId)).willReturn(java.util.Optional.of(place));
                given(postPlaceRepository.findAllByPlaceId(placeId)).willReturn(List.of(archivedPostPlace, nonArchivedPostPlace));
                given(archiverVisibilityService.getVisibilityFilter(archiverId))
                                .willReturn(new ArchiverVisibilityService.VisibilityFilter(java.util.Set.of(), java.util.Set.of()));
                given(archiverVisibilityService.filterVisiblePostPlaces(
                                org.mockito.ArgumentMatchers.eq(List.of(archivedPostPlace, nonArchivedPostPlace)),
                                org.mockito.ArgumentMatchers.any()))
                                .willReturn(List.of(archivedPostPlace, nonArchivedPostPlace));
                given(userClient.getEditorSummaries(List.of(editorId)))
                                .willReturn(java.util.Map.of(
                                                editorId,
                                                new UserClient.EditorSummary(editorId, "에디터", "editor_insta")));
                given(postPlaceSaveRepository.findAllByArchiverIdAndPostPlaceIdIn(archiverId, List.of(100L, 101L)))
                                .willReturn(List.of(PostPlaceArchive.createOf(archiverId, 100L)));

                ArchiverPlaceDetailDto.Response response = postQueryService.getArchiverPlaceDetail(placeId, archiverId);

                assertThat(response.getPostPlaces()).hasSize(2);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(100L);
                assertThat(response.getPostPlaces().get(0).isArchived()).isTrue();
                assertThat(response.getPostPlaces().get(1).getPostPlaceId()).isEqualTo(101L);
                assertThat(response.getPostPlaces().get(1).isArchived()).isFalse();
        }

        @Test
        @DisplayName("핫플 조회 - 완비 카드가 있으면 최신 완비 카드를 선택한다")
        void getHotPlaces_prefersLatestCompletePostPlace() {
                Place place = Place.builder().id(1L).name("성수 핫플").viewCount(100L).build();
                Category cafe = Category.builder().id(1L).name("카페").build();

                Post completePost = Post.createOf(UUID.randomUUID(), "https://www.instagram.com/p/complete", List.of("#완비"));
                Post incompletePost = Post.createOf(UUID.randomUUID(), "https://www.instagram.com/p/latest", List.of("#최신"));

                PostPlace complete = PostPlace.builder()
                                .id(11L)
                                .post(completePost)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("완비 설명")
                                .imageUrl("https://img.complete")
                                .build();
                complete.addCategory(cafe);
                setField(complete, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 10, 0, 0));

                PostPlace latestIncomplete = PostPlace.builder()
                                .id(12L)
                                .post(incompletePost)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("최신 설명")
                                .imageUrl("https://img.latest")
                                .build();
                setField(latestIncomplete, "lastModifiedAt", LocalDateTime.of(2026, 2, 12, 10, 0, 0));

                given(placeRepository.findTopByViewCount(10)).willReturn(List.of(place));
                given(postPlaceRepository.findAllByPlaceIds(List.of(1L))).willReturn(List.of(latestIncomplete, complete));

                ArchiverHotPlaceDto.ListResponse response = postQueryService.getHotPlaces(10, null);

                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getImageUrl()).isEqualTo("https://img.complete");
                assertThat(response.getPlaces().get(0).getCategoryIds()).containsExactly(1L);
                assertThat(response.getPlaces().get(0).getHashTags()).containsExactly("#완비");
        }

        @Test
        @DisplayName("핫플 조회 - 완비 카드가 여러 개면 가장 최근 완비 카드를 선택한다")
        void getHotPlaces_selectsLatestAmongCompleteCandidates() {
                Place place = Place.builder().id(1L).name("성수 핫플").viewCount(100L).build();
                Category cafe = Category.builder().id(1L).name("카페").build();

                Post oldPost = Post.createOf(UUID.randomUUID(), "https://www.instagram.com/p/old", List.of("#오래됨"));
                Post newPost = Post.createOf(UUID.randomUUID(), "https://www.instagram.com/p/new", List.of("#최신완비"));

                PostPlace oldComplete = PostPlace.builder()
                                .id(21L)
                                .post(oldPost)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("오래된 완비")
                                .imageUrl("https://img.old")
                                .build();
                oldComplete.addCategory(cafe);
                setField(oldComplete, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 10, 0, 0));

                PostPlace newComplete = PostPlace.builder()
                                .id(22L)
                                .post(newPost)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("최근 완비")
                                .imageUrl("https://img.new")
                                .build();
                newComplete.addCategory(cafe);
                setField(newComplete, "lastModifiedAt", LocalDateTime.of(2026, 2, 12, 10, 0, 0));

                given(placeRepository.findTopByViewCount(10)).willReturn(List.of(place));
                given(postPlaceRepository.findAllByPlaceIds(List.of(1L))).willReturn(List.of(oldComplete, newComplete));

                ArchiverHotPlaceDto.ListResponse response = postQueryService.getHotPlaces(10, null);

                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getImageUrl()).isEqualTo("https://img.new");
                assertThat(response.getPlaces().get(0).getHashTags()).containsExactly("#최신완비");
        }

        @Test
        @DisplayName("핫플 조회 - 완비 카드가 없으면 최신 카드로 대체한다")
        void getHotPlaces_fallsBackToLatestWhenNoCompleteCard() {
                Place place = Place.builder().id(1L).name("성수 핫플").viewCount(100L).build();
                Post latestPost = Post.createOf(UUID.randomUUID(), "https://www.instagram.com/p/latest", List.of("#최신"));
                Post oldPost = Post.createOf(UUID.randomUUID(), "https://www.instagram.com/p/old", List.of("#오래됨"));

                PostPlace latestIncomplete = PostPlace.builder()
                                .id(31L)
                                .post(latestPost)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("최신 설명")
                                .imageUrl("https://img.latest")
                                .build();
                setField(latestIncomplete, "lastModifiedAt", LocalDateTime.of(2026, 2, 12, 10, 0, 0));

                PostPlace olderIncomplete = PostPlace.builder()
                                .id(32L)
                                .post(oldPost)
                                .place(place)
                                .editorId(UUID.randomUUID())
                                .description("오래된 설명")
                                .imageUrl("")
                                .build();
                setField(olderIncomplete, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 10, 0, 0));

                given(placeRepository.findTopByViewCount(10)).willReturn(List.of(place));
                given(postPlaceRepository.findAllByPlaceIds(List.of(1L))).willReturn(List.of(olderIncomplete, latestIncomplete));

                ArchiverHotPlaceDto.ListResponse response = postQueryService.getHotPlaces(10, null);

                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getPlaces().get(0).getImageUrl()).isEqualTo("https://img.latest");
                assertThat(response.getPlaces().get(0).getHashTags()).containsExactly("#최신");
                assertThat(response.getPlaces().get(0).getCategoryIds()).isEmpty();
        }

        @Test
        @DisplayName("아카이버가 조회하는 에디터 업로드 postPlace 목록 - LATEST 정렬")
        void getEditorUploadedPostPlaces_latest_success() {
                UUID editorId = UUID.randomUUID();
                Category korean = Category.builder().id(1L).name("한식").build();
                Category western = Category.builder().id(2L).name("양식").build();

                Place place1 = Place.builder().id(1L).name("최근 장소").build();
                Place place2 = Place.builder().id(2L).name("오래된 장소").build();
                Post post = Post.builder().id(1L).build();

                PostPlace latest = PostPlace.builder()
                                .id(11L)
                                .post(post)
                                .place(place1)
                                .editorId(editorId)
                                .description("최근 설명")
                                .saveCount(5L)
                                .viewCount(50L)
                                .imageUrl("https://img1")
                                .build();
                latest.addCategory(korean);
                latest.addCategory(western);
                PostPlace oldest = PostPlace.builder()
                                .id(12L)
                                .post(post)
                                .place(place2)
                                .editorId(editorId)
                                .description("오래된 설명")
                                .saveCount(1L)
                                .viewCount(10L)
                                .imageUrl("https://img2")
                                .build();
                oldest.addCategory(korean);

                setField(latest, "lastModifiedAt", LocalDateTime.of(2026, 2, 9, 12, 0, 0));
                setField(oldest, "lastModifiedAt", LocalDateTime.of(2026, 2, 1, 12, 0, 0));
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(oldest, latest));

                ArchiverEditorPostPlaceDto.ListResponse response = postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.LATEST);

                assertThat(response.getTotalCount()).isEqualTo(2L);
                assertThat(response.getPostPlaces()).hasSize(2);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(11L);
                assertThat(response.getPostPlaces().get(0).getCategoryIds()).containsExactly(1L, 2L);
                assertThat(response.getPostPlaces().get(1).getPostPlaceId()).isEqualTo(12L);
        }

        @Test
        @DisplayName("아카이버가 조회하는 에디터 업로드 postPlace 목록 - OLDEST 정렬")
        void getEditorUploadedPostPlaces_oldest_success() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);

                Place place1 = Place.builder().id(1L).name("최근 장소").build();
                Place place2 = Place.builder().id(2L).name("오래된 장소").build();
                Post post = Post.builder().id(1L).build();

                PostPlace latest = PostPlace.builder().id(21L).post(post).place(place1).editorId(editorId).build();
                PostPlace oldest = PostPlace.builder().id(22L).post(post).place(place2).editorId(editorId).build();
                setField(latest, "lastModifiedAt", LocalDateTime.of(2026, 2, 9, 12, 0, 0));
                setField(oldest, "lastModifiedAt", LocalDateTime.of(2026, 2, 1, 12, 0, 0));
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(latest, oldest));

                ArchiverEditorPostPlaceDto.ListResponse response = postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.OLDEST);

                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(22L);
                assertThat(response.getPostPlaces().get(1).getPostPlaceId()).isEqualTo(21L);
        }

        @Test
        @DisplayName("아카이버가 조회하는 에디터 업로드 postPlace 목록 - 수정시각 없으면 생성시각 사용")
        void getEditorUploadedPostPlaces_fallbackCreatedAt_success() {
                UUID editorId = UUID.randomUUID();
                Category cafe = Category.builder().id(1L).name("카페").build();

                Place place = Place.builder().id(1L).name("장소").build();
                Post post = Post.builder().id(1L).build();
                PostPlace postPlace = PostPlace.builder()
                                .id(31L)
                                .post(post)
                                .place(place)
                                .editorId(editorId)
                                .saveCount(null)
                                .viewCount(null)
                                .build();
                postPlace.addCategory(cafe);
                setField(postPlace, "createdAt", LocalDateTime.of(2026, 2, 3, 8, 30, 0));
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(postPlace));

                ArchiverEditorPostPlaceDto.ListResponse response = postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.LATEST);

                assertThat(response.getPostPlaces().get(0).getLastModifiedAt())
                                .isEqualTo(LocalDateTime.of(2026, 2, 3, 8, 30, 0));
                assertThat(response.getPostPlaces().get(0).getSaveCount()).isEqualTo(0L);
                assertThat(response.getPostPlaces().get(0).getViewCount()).isEqualTo(0L);
                assertThat(response.getPostPlaces().get(0).getCategoryIds()).containsExactly(1L);
        }

        @Test
        @DisplayName("아카이버가 조회하는 에디터 업로드 postPlace 목록 - 에디터가 없으면 예외")
        void getEditorUploadedPostPlaces_editorNotFound_throwsException() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(false);

                assertThatThrownBy(() -> postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.LATEST))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.POST_EDITOR_PROFILE_NOT_FOUND);
        }

        @Test
        @DisplayName("아카이버가 조회하는 에디터 업로드 postPlace 목록 - 비어있으면 빈 응답")
        void getEditorUploadedPostPlaces_empty_success() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of());

                ArchiverEditorPostPlaceDto.ListResponse response = postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.LATEST);

                assertThat(response.getTotalCount()).isEqualTo(0L);
                assertThat(response.getPostPlaces()).isEmpty();
        }

        @Test
        @DisplayName("아카이버용 에디터 지도 핀 조회 - 카테고리 AND 필터")
        void getMapPinsForArchiver_categoryAndFilter_success() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);

                Category korean = Category.builder().id(1L).name("한식").build();
                Category western = Category.builder().id(2L).name("양식").build();
                Category japanese = Category.builder().id(3L).name("일식").build();

                Place bothCategoryPlace = Place.builder()
                                .id(1L)
                                .name("한양식당")
                                .position(Position.of(37.5445, 127.0560))
                                .build();
                Place singleCategoryPlace = Place.builder()
                                .id(2L)
                                .name("일식당")
                                .position(Position.of(37.5450, 127.0565))
                                .build();
                Post post = Post.builder().id(1L).build();

                PostPlace pp1 = PostPlace.builder().id(1L).post(post).place(bothCategoryPlace).editorId(editorId).build();
                pp1.addCategory(korean);
                pp1.addCategory(western);

                PostPlace pp2 = PostPlace.builder().id(2L).post(post).place(singleCategoryPlace).editorId(editorId).build();
                pp2.addCategory(japanese);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(bothCategoryPlace, singleCategoryPlace));

                EditorMapDto.Response response = postQueryService.getMapPinsForArchiver(
                                editorId,
                                MapFilter.ALL,
                                List.of(1L, 2L),
                                null,
                                null);

                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getPlaceId()).isEqualTo(1L);
                assertThat(response.getPins().get(0).getName()).isEqualTo("한양식당");
        }

        @Test
        @DisplayName("아카이버용 에디터 지도 핀 조회 - NEARBY와 카테고리 동시 AND")
        void getMapPinsForArchiver_nearbyAndCategory_success() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);

                Category korean = Category.builder().id(1L).name("한식").build();
                Category western = Category.builder().id(2L).name("양식").build();

                Place nearPlace = Place.builder()
                                .id(10L)
                                .name("근처 한양식")
                                .position(Position.of(37.5449, 127.0562))
                                .build();
                Place farPlace = Place.builder()
                                .id(11L)
                                .name("먼 한양식")
                                .position(Position.of(37.5649, 127.0762))
                                .build();
                Post post = Post.builder().id(1L).build();

                PostPlace nearPp = PostPlace.builder().id(10L).post(post).place(nearPlace).editorId(editorId).build();
                nearPp.addCategory(korean);
                nearPp.addCategory(western);

                PostPlace farPp = PostPlace.builder().id(11L).post(post).place(farPlace).editorId(editorId).build();
                farPp.addCategory(korean);
                farPp.addCategory(western);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(nearPp, farPp));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(nearPlace, farPlace));

                EditorMapDto.Response response = postQueryService.getMapPinsForArchiver(
                                editorId,
                                MapFilter.NEARBY,
                                List.of(1L, 2L),
                                37.5445,
                                127.0560);

                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getPlaceId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("아카이버용 에디터 지도 핀 조회 - 에디터가 없으면 예외")
        void getMapPinsForArchiver_editorNotFound_throwsException() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(false);

                assertThatThrownBy(() -> postQueryService.getMapPinsForArchiver(
                                editorId,
                                MapFilter.ALL,
                                null,
                                null,
                                null))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.POST_EDITOR_PROFILE_NOT_FOUND);
        }

        @Test
        @DisplayName("아카이버용 에디터 지도 핀 조회 - NEARBY 좌표 누락 시 예외")
        void getMapPinsForArchiver_nearbyWithoutCoordinates_throwsException() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);

                assertThatThrownBy(() -> postQueryService.getMapPinsForArchiver(
                                editorId,
                                MapFilter.NEARBY,
                                null,
                                null,
                                127.0560))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.INVALID_POSITION_LATITUDE);
        }

        @Test
        @DisplayName("아카이버용 에디터 지도 핀 조회 - postPlace 없으면 빈 응답")
        void getMapPinsForArchiver_empty_success() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of());

                EditorMapDto.Response response = postQueryService.getMapPinsForArchiver(
                                editorId,
                                MapFilter.ALL,
                                List.of(1L, 2L),
                                null,
                                null);

                assertThat(response.getPins()).isEmpty();
        }

        @Test
        @DisplayName("아카이버 아카이브 목록 조회 - 최근 저장순으로 조회하고 placeName을 포함한다")
        void getMyArchivedPostPlaces_success() {
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();
                Place place1 = Place.builder().id(101L).name("성수 카페").build();
                Place place2 = Place.builder().id(102L).name("연남 식당").build();
                PostPlace pp1 = PostPlace.builder()
                                .id(11L)
                                .post(post)
                                .place(place1)
                                .editorId(editorId)
                                .description("설명1")
                                .saveCount(5L)
                                .viewCount(10L)
                                .build();
                PostPlace pp2 = PostPlace.builder()
                                .id(12L)
                                .post(post)
                                .place(place2)
                                .editorId(editorId)
                                .description("설명2")
                                .saveCount(7L)
                                .viewCount(20L)
                                .build();

                PostPlaceArchive save2 = PostPlaceArchive.builder().id(2L).archiverId(archiverId).postPlaceId(12L).build();
                PostPlaceArchive save1 = PostPlaceArchive.builder().id(1L).archiverId(archiverId).postPlaceId(11L).build();
                setField(save2, "createdAt", LocalDateTime.of(2026, 2, 11, 10, 0, 0));
                setField(save1, "createdAt", LocalDateTime.of(2026, 2, 10, 10, 0, 0));
                setField(pp2, "lastModifiedAt", LocalDateTime.of(2026, 2, 9, 12, 0, 0));
                setField(pp1, "lastModifiedAt", LocalDateTime.of(2026, 2, 8, 12, 0, 0));

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = new ArchiverVisibilityService.VisibilityFilter(
                                java.util.Set.of(),
                                java.util.Set.of());
                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId))
                                .willReturn(List.of(save2, save1));
                given(postPlaceRepository.findAllByIds(List.of(12L, 11L))).willReturn(List.of(pp1, pp2));
                given(archiverVisibilityService.getVisibilityFilter(archiverId)).willReturn(visibilityFilter);
                given(archiverVisibilityService.isVisible(pp1, visibilityFilter)).willReturn(true);
                given(archiverVisibilityService.isVisible(pp2, visibilityFilter)).willReturn(true);

                ArchiverArchivedPostPlaceDto.ListResponse response = postQueryService.getMyArchivedPostPlaces(
                                MapFilter.ALL,
                                null,
                                null,
                                archiverId);

                assertThat(response.getTotalCount()).isEqualTo(2L);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(12L);
                assertThat(response.getPostPlaces().get(0).getPlaceName()).isEqualTo("연남 식당");
                assertThat(response.getPostPlaces().get(1).getPostPlaceId()).isEqualTo(11L);
                assertThat(response.getPostPlaces().get(1).getPlaceName()).isEqualTo("성수 카페");
        }

        @Test
        @DisplayName("아카이버 아카이브 목록 조회 - 가시성/누락된 postPlace는 제외한다")
        void getMyArchivedPostPlaces_filtersInvisibleAndMissing() {
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();
                Place place = Place.builder().id(101L).name("성수 카페").build();
                PostPlace visible = PostPlace.builder().id(11L).post(post).place(place).editorId(editorId).build();
                PostPlace hidden = PostPlace.builder().id(12L).post(post).place(place).editorId(editorId).build();

                PostPlaceArchive saveVisible = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(11L).build();
                PostPlaceArchive saveHidden = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(12L).build();
                PostPlaceArchive saveMissing = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(13L).build();

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = new ArchiverVisibilityService.VisibilityFilter(
                                java.util.Set.of(),
                                java.util.Set.of());
                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId))
                                .willReturn(List.of(saveVisible, saveHidden, saveMissing));
                given(postPlaceRepository.findAllByIds(List.of(11L, 12L, 13L))).willReturn(List.of(visible, hidden));
                given(archiverVisibilityService.getVisibilityFilter(archiverId)).willReturn(visibilityFilter);
                given(archiverVisibilityService.isVisible(visible, visibilityFilter)).willReturn(true);
                given(archiverVisibilityService.isVisible(hidden, visibilityFilter)).willReturn(false);

                ArchiverArchivedPostPlaceDto.ListResponse response = postQueryService.getMyArchivedPostPlaces(
                                MapFilter.ALL,
                                null,
                                null,
                                archiverId);

                assertThat(response.getTotalCount()).isEqualTo(1L);
                assertThat(response.getPostPlaces()).hasSize(1);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(11L);
        }

        @Test
        @DisplayName("아카이버 아카이브 목록 조회 - 저장 목록이 비어있으면 빈 응답")
        void getMyArchivedPostPlaces_empty() {
                UUID archiverId = UUID.randomUUID();
                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId)).willReturn(List.of());

                ArchiverArchivedPostPlaceDto.ListResponse response = postQueryService.getMyArchivedPostPlaces(
                                MapFilter.ALL,
                                null,
                                null,
                                archiverId);

                assertThat(response.getTotalCount()).isEqualTo(0L);
                assertThat(response.getPostPlaces()).isEmpty();
        }

        @Test
        @DisplayName("아카이버 아카이브 목록 조회 - NEARBY 필터를 적용한다")
        void getMyArchivedPostPlaces_nearbyFilter() {
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();

                Place nearPlace = Place.builder()
                                .id(101L)
                                .name("근처 카페")
                                .position(Position.of(37.5449, 127.0562))
                                .build();
                Place farPlace = Place.builder()
                                .id(102L)
                                .name("먼 카페")
                                .position(Position.of(37.5649, 127.0762))
                                .build();

                PostPlace nearPostPlace = PostPlace.builder().id(11L).post(post).place(nearPlace).editorId(editorId).build();
                PostPlace farPostPlace = PostPlace.builder().id(12L).post(post).place(farPlace).editorId(editorId).build();
                PostPlaceArchive saveNear = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(11L).build();
                PostPlaceArchive saveFar = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(12L).build();

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = new ArchiverVisibilityService.VisibilityFilter(
                                java.util.Set.of(),
                                java.util.Set.of());
                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId))
                                .willReturn(List.of(saveNear, saveFar));
                given(postPlaceRepository.findAllByIds(List.of(11L, 12L))).willReturn(List.of(nearPostPlace, farPostPlace));
                given(archiverVisibilityService.getVisibilityFilter(archiverId)).willReturn(visibilityFilter);
                given(archiverVisibilityService.isVisible(nearPostPlace, visibilityFilter)).willReturn(true);

                ArchiverArchivedPostPlaceDto.ListResponse response = postQueryService.getMyArchivedPostPlaces(
                                MapFilter.NEARBY,
                                37.5445,
                                127.0560,
                                archiverId);

                assertThat(response.getTotalCount()).isEqualTo(1L);
                assertThat(response.getPostPlaces()).hasSize(1);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(11L);
        }

        @Test
        @DisplayName("아카이버 아카이브 목록 조회 - NEARBY 좌표 누락 시 예외")
        void getMyArchivedPostPlaces_nearbyWithoutCoordinates_throwsException() {
                UUID archiverId = UUID.randomUUID();

                assertThatThrownBy(() -> postQueryService.getMyArchivedPostPlaces(
                                MapFilter.NEARBY,
                                null,
                                127.0560,
                                archiverId))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.INVALID_POSITION_LATITUDE);
        }

        @Test
        @DisplayName("아카이버 아카이브 지도 핀 조회 - 근처 필터를 적용한다")
        void getMyArchivedMapPins_success() {
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();
                Category korean = Category.builder().id(1L).name("한식").build();
                Category western = Category.builder().id(2L).name("양식").build();

                Place nearPlace = Place.builder()
                                .id(11L)
                                .name("근처 한양식")
                                .position(Position.of(37.5449, 127.0562))
                                .build();
                Place farPlace = Place.builder()
                                .id(12L)
                                .name("먼 한양식")
                                .position(Position.of(37.5649, 127.0762))
                                .build();
                Post post = Post.builder().id(1L).build();
                PostPlace nearPostPlace = PostPlace.builder().id(101L).post(post).place(nearPlace).editorId(editorId).build();
                nearPostPlace.addCategory(korean);
                nearPostPlace.addCategory(western);
                PostPlace farPostPlace = PostPlace.builder().id(102L).post(post).place(farPlace).editorId(editorId).build();
                farPostPlace.addCategory(korean);
                farPostPlace.addCategory(western);

                PostPlaceArchive saveNear = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(101L).build();
                PostPlaceArchive saveFar = PostPlaceArchive.builder().archiverId(archiverId).postPlaceId(102L).build();
                ArchiverVisibilityService.VisibilityFilter visibilityFilter = new ArchiverVisibilityService.VisibilityFilter(
                                java.util.Set.of(),
                                java.util.Set.of());

                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId))
                                .willReturn(List.of(saveNear, saveFar));
                given(postPlaceRepository.findAllByIds(List.of(101L, 102L))).willReturn(List.of(nearPostPlace, farPostPlace));
                given(archiverVisibilityService.getVisibilityFilter(archiverId)).willReturn(visibilityFilter);
                given(archiverVisibilityService.filterVisiblePostPlaces(List.of(nearPostPlace, farPostPlace), visibilityFilter))
                                .willReturn(List.of(nearPostPlace, farPostPlace));
                given(placeRepository.findAllByIds(List.of(11L, 12L))).willReturn(List.of(nearPlace, farPlace));

                EditorMapDto.Response response = postQueryService.getMyArchivedMapPins(
                                MapFilter.NEARBY,
                                37.5445,
                                127.0560,
                                archiverId);

                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getPlaceId()).isEqualTo(11L);
                assertThat(response.getPins().get(0).getName()).isEqualTo("근처 한양식");
        }

        @Test
        @DisplayName("아카이버 아카이브 지도 핀 조회 - NEARBY 좌표 누락 시 예외")
        void getMyArchivedMapPins_nearbyWithoutCoordinates_throwsException() {
                UUID archiverId = UUID.randomUUID();

                assertThatThrownBy(() -> postQueryService.getMyArchivedMapPins(
                                MapFilter.NEARBY,
                                null,
                                127.0560,
                                archiverId))
                                .isInstanceOf(DomainException.class)
                                .extracting(ex -> ((DomainException) ex).getErrorCode())
                                .isEqualTo(PostErrorCode.INVALID_POSITION_LATITUDE);
        }

        private static void setField(Object target, String fieldName, Object value) {
                try {
                        Class<?> type = target.getClass();
                        while (type != null) {
                                try {
                                        java.lang.reflect.Field field = type.getDeclaredField(fieldName);
                                        field.setAccessible(true);
                                        field.set(target, value);
                                        return;
                                } catch (NoSuchFieldException ignored) {
                                        type = type.getSuperclass();
                                }
                        }
                        throw new NoSuchFieldException(fieldName);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                }
        }
}
