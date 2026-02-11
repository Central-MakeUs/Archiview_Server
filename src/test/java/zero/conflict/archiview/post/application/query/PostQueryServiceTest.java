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
import zero.conflict.archiview.post.application.port.out.PostPlaceSaveRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.post.application.archiver.query.PostQueryService;
import zero.conflict.archiview.post.domain.*;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverSavedPostPlaceDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
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
        private PostPlaceSaveRepository postPlaceSaveRepository;

        @Mock
        private UserClient userClient;

        @Mock
        private CategoryPlaceReadRepository categoryPlaceReadRepository;

        @Mock
        private ArchiverVisibilityService archiverVisibilityService;

        @Mock
        private CategoryPlaceReadRepository.CategoryPlaceSummaryProjection categoryPlaceSummaryProjection;

        @Test
        @DisplayName("좌표 기준 1km 내 장소 목록을 조회한다")
        void getNearbyPlacesWithin1km_success() {
                // given
                Double latitude = 37.5445;
                Double longitude = 127.0560;
                given(categoryPlaceReadRepository.findPlaceSummariesNearby(latitude, longitude, 1000))
                                .willReturn(List.of(categoryPlaceSummaryProjection));
                given(categoryPlaceSummaryProjection.getPlaceId()).willReturn(11L);
                given(categoryPlaceSummaryProjection.getPlaceName()).willReturn("성수 카페");
                given(categoryPlaceSummaryProjection.getLatestDescription()).willReturn("최근 설명");
                given(categoryPlaceSummaryProjection.getViewCount()).willReturn(100L);
                given(categoryPlaceSummaryProjection.getSaveCount()).willReturn(20L);

                // when
                CategoryQueryDto.CategoryPlaceListResponse response = postQueryService
                                .getNearbyPlacesWithin1km(latitude, longitude);

                // then
                assertThat(response.getPlaces()).hasSize(1);
                assertThat(response.getTotalCount()).isEqualTo(1L);
                assertThat(response.getPlaces().get(0).getPlaceId()).isEqualTo(11L);
                assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("성수 카페");
                assertThat(response.getPlaces().get(0).getSaveCount()).isEqualTo(20L);
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
                                null);

                // then
                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getName()).isEqualTo("식당");
                assertThat(response.getPins().get(0).getCategories()).containsExactly("한식");
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
                                List.of(category1.getId()));

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

                // when (Nearby filter now returns all pins as BBox is removed)
                EditorMapDto.Response response = editorPostQueryService.getMapPins(
                                editorId,
                                MapFilter.NEARBY,
                                null);

                // then
                assertThat(response.getPins()).hasSize(2); // Both near and far pins returned
                assertThat(response.getPins()).extracting("name")
                                .containsExactlyInAnyOrder("가까운곳", "먼곳");
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

                given(postPlaceRepository.findAllByEditorIdAndPlaceId(editorId, placeId))
                                .willReturn(List.of(postPlace));
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
                assertThat(response.getPostPlaces()).hasSize(1);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(100L);
                assertThat(response.getPostPlaces().get(0).getEditorName()).isEqualTo("에디터");
                assertThat(response.getPostPlaces().get(0).getEditorInstagramId()).isEqualTo("editor_insta");
        }

        @Test
        @DisplayName("아카이버가 조회하는 에디터 업로드 postPlace 목록 - LATEST 정렬")
        void getEditorUploadedPostPlaces_latest_success() {
                UUID editorId = UUID.randomUUID();
                given(userClient.existsEditorProfile(editorId)).willReturn(true);

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

                setField(latest, "lastModifiedAt", LocalDateTime.of(2026, 2, 9, 12, 0, 0));
                setField(oldest, "lastModifiedAt", LocalDateTime.of(2026, 2, 1, 12, 0, 0));
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(oldest, latest));

                ArchiverEditorPostPlaceDto.ListResponse response = postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.LATEST);

                assertThat(response.getTotalCount()).isEqualTo(2L);
                assertThat(response.getPostPlaces()).hasSize(2);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(11L);
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
                given(userClient.existsEditorProfile(editorId)).willReturn(true);

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
                setField(postPlace, "createdAt", LocalDateTime.of(2026, 2, 3, 8, 30, 0));
                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(postPlace));

                ArchiverEditorPostPlaceDto.ListResponse response = postQueryService.getEditorUploadedPostPlaces(
                                editorId,
                                ArchiverEditorPostPlaceDto.Sort.LATEST);

                assertThat(response.getPostPlaces().get(0).getLastModifiedAt())
                                .isEqualTo(LocalDateTime.of(2026, 2, 3, 8, 30, 0));
                assertThat(response.getPostPlaces().get(0).getSaveCount()).isEqualTo(0L);
                assertThat(response.getPostPlaces().get(0).getViewCount()).isEqualTo(0L);
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
        @DisplayName("아카이버 저장 목록 조회 - 최근 저장순으로 조회하고 placeName을 포함한다")
        void getMySavedPostPlaces_success() {
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

                PostPlaceSave save2 = PostPlaceSave.builder().id(2L).archiverId(archiverId).postPlaceId(12L).build();
                PostPlaceSave save1 = PostPlaceSave.builder().id(1L).archiverId(archiverId).postPlaceId(11L).build();
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

                ArchiverSavedPostPlaceDto.ListResponse response = postQueryService.getMySavedPostPlaces(archiverId);

                assertThat(response.getTotalCount()).isEqualTo(2L);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(12L);
                assertThat(response.getPostPlaces().get(0).getPlaceName()).isEqualTo("연남 식당");
                assertThat(response.getPostPlaces().get(1).getPostPlaceId()).isEqualTo(11L);
                assertThat(response.getPostPlaces().get(1).getPlaceName()).isEqualTo("성수 카페");
        }

        @Test
        @DisplayName("아카이버 저장 목록 조회 - 가시성/누락된 postPlace는 제외한다")
        void getMySavedPostPlaces_filtersInvisibleAndMissing() {
                UUID archiverId = UUID.randomUUID();
                UUID editorId = UUID.randomUUID();
                Post post = Post.builder().id(1L).build();
                Place place = Place.builder().id(101L).name("성수 카페").build();
                PostPlace visible = PostPlace.builder().id(11L).post(post).place(place).editorId(editorId).build();
                PostPlace hidden = PostPlace.builder().id(12L).post(post).place(place).editorId(editorId).build();

                PostPlaceSave saveVisible = PostPlaceSave.builder().archiverId(archiverId).postPlaceId(11L).build();
                PostPlaceSave saveHidden = PostPlaceSave.builder().archiverId(archiverId).postPlaceId(12L).build();
                PostPlaceSave saveMissing = PostPlaceSave.builder().archiverId(archiverId).postPlaceId(13L).build();

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = new ArchiverVisibilityService.VisibilityFilter(
                                java.util.Set.of(),
                                java.util.Set.of());
                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId))
                                .willReturn(List.of(saveVisible, saveHidden, saveMissing));
                given(postPlaceRepository.findAllByIds(List.of(11L, 12L, 13L))).willReturn(List.of(visible, hidden));
                given(archiverVisibilityService.getVisibilityFilter(archiverId)).willReturn(visibilityFilter);
                given(archiverVisibilityService.isVisible(visible, visibilityFilter)).willReturn(true);
                given(archiverVisibilityService.isVisible(hidden, visibilityFilter)).willReturn(false);

                ArchiverSavedPostPlaceDto.ListResponse response = postQueryService.getMySavedPostPlaces(archiverId);

                assertThat(response.getTotalCount()).isEqualTo(1L);
                assertThat(response.getPostPlaces()).hasSize(1);
                assertThat(response.getPostPlaces().get(0).getPostPlaceId()).isEqualTo(11L);
        }

        @Test
        @DisplayName("아카이버 저장 목록 조회 - 저장 목록이 비어있으면 빈 응답")
        void getMySavedPostPlaces_empty() {
                UUID archiverId = UUID.randomUUID();
                given(postPlaceSaveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId)).willReturn(List.of());

                ArchiverSavedPostPlaceDto.ListResponse response = postQueryService.getMySavedPostPlaces(archiverId);

                assertThat(response.getTotalCount()).isEqualTo(0L);
                assertThat(response.getPostPlaces()).isEmpty();
        }

        @Test
        @DisplayName("아카이버 저장 지도 핀 조회 - 카테고리/근처 필터를 적용한다")
        void getMySavedMapPins_success() {
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

                PostPlaceSave saveNear = PostPlaceSave.builder().archiverId(archiverId).postPlaceId(101L).build();
                PostPlaceSave saveFar = PostPlaceSave.builder().archiverId(archiverId).postPlaceId(102L).build();
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

                EditorMapDto.Response response = postQueryService.getMySavedMapPins(
                                MapFilter.NEARBY,
                                List.of(1L, 2L),
                                37.5445,
                                127.0560,
                                archiverId);

                assertThat(response.getPins()).hasSize(1);
                assertThat(response.getPins().get(0).getPlaceId()).isEqualTo(11L);
                assertThat(response.getPins().get(0).getName()).isEqualTo("근처 한양식");
        }

        @Test
        @DisplayName("아카이버 저장 지도 핀 조회 - NEARBY 좌표 누락 시 예외")
        void getMySavedMapPins_nearbyWithoutCoordinates_throwsException() {
                UUID archiverId = UUID.randomUUID();

                assertThatThrownBy(() -> postQueryService.getMySavedMapPins(
                                MapFilter.NEARBY,
                                null,
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
