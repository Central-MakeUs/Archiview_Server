package zero.conflict.archiview.post.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.post.application.editor.command.PostCommandService;
import zero.conflict.archiview.post.application.editor.command.event.PostOutboxService;
import zero.conflict.archiview.post.domain.*;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostCommandService 테스트")
class PostCommandServiceTest {

        @InjectMocks
        private PostCommandService postCommandService;

        @Mock
        private PostRepository postRepository;

        @Mock
        private PlaceRepository placeRepository;

        @Mock
        private PostPlaceRepository postPlacesRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private UserClient userClient;

        @Mock
        private zero.conflict.archiview.global.infra.s3.S3Service s3Service;

        @Mock
        private PostOutboxService postOutboxService;

        @Test
        @DisplayName("Post 생성 시 Place가 새로 생성되어야 한다")
        void createPost_withNewPlace_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                String url = "https://www.instagram.com/post1";
                java.util.List<String> hashTags = java.util.List.of("#여행", "#맛집");

                PostCommandDto.Request.PlaceInfoRequest placeInfoRequest = PostCommandDto.Request.PlaceInfoRequest
                                .builder()
                                .placeName("테스트 장소")
                                .addressName("서울 노원구 공릉동 596-12")
                                .roadAddressName("인천 중구 백운로228번길 81-10")
                                .description("멋진 장소")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 5분")
                                .phoneNumber("02-1234-5678")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url(url)
                                .hashTags(hashTags)
                                .placeInfoRequestList(List.of(placeInfoRequest))
                                .build();

                Post savedPost = Post.createOf(editorId, url, hashTags);
                given(postRepository.save(any(Post.class))).willReturn(savedPost);

                Place newPlace = Place.createOf(
                                placeInfoRequest.getPlaceName(),
                                Address.of(placeInfoRequest.getAddressName(), placeInfoRequest.getRoadAddressName()),
                                Position.of(placeInfoRequest.getLatitude(), placeInfoRequest.getLongitude()),
                                placeInfoRequest.getNearestStationWalkTime(),
                                null,
                                placeInfoRequest.getPhoneNumber());
                given(placeRepository.findByPosition(any(Position.class))).willReturn(Optional.empty());
                given(placeRepository.save(any(Place.class))).willReturn(newPlace);

                given(userClient.existsUser(editorId)).willReturn(true);

                PostPlace postPlace = PostPlace.createOf(savedPost, newPlace,
                                placeInfoRequest.getDescription(),
                                null, editorId);
                given(postPlacesRepository.save(any(PostPlace.class))).willReturn(postPlace);

                // when
                PostCommandDto.Response response = postCommandService.createPost(request, editorId);

                // then
                assertThat(response.getUrl()).isEqualTo(url);
                assertThat(response.getHashTags()).isEqualTo(hashTags);
                assertThat(response.getPlaceInfoResponseList()).hasSize(1);
                assertThat(response.getPlaceInfoResponseList().get(0).getName()).isEqualTo("테스트 장소");
                assertThat(response.getPlaceInfoResponseList().get(0).getPhoneNumber()).isEqualTo("02-1234-5678");

                verify(postRepository).save(any(Post.class));
                verify(placeRepository).findByPosition(any(Position.class));
                verify(placeRepository).save(argThat(place -> "도보 5분".equals(place.getNearestStationWalkTime())));
                verify(postPlacesRepository).save(any(PostPlace.class));
        }

        @Test
        @DisplayName("Post 생성 시 중복된 위치의 Place는 재사용해야 한다")
        void createPost_withExistingPlace_reusePlace() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                String url = "https://www.instagram.com/post2";
                java.util.List<String> hashTags = java.util.List.of("#맛집", "#데이트");

                PostCommandDto.Request.PlaceInfoRequest placeInfoRequest = PostCommandDto.Request.PlaceInfoRequest
                                .builder()
                                .placeName("기존 장소")
                                .addressName("서울시 종로구 묘동 123-45")
                                .roadAddressName("서울시 종로구 묘동길 1")
                                .description("이미 존재하는 장소")
                                .latitude(Double.valueOf("37.5700"))
                                .longitude(Double.valueOf("126.9800"))
                                .nearestStationWalkTime("도보 3분")
                                .phoneNumber("02-1234-0000")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url(url)
                                .hashTags(hashTags)
                                .placeInfoRequestList(List.of(placeInfoRequest))
                                .build();

                Post savedPost = Post.createOf(editorId, url, hashTags);
                given(postRepository.save(any(Post.class))).willReturn(savedPost);

                given(userClient.existsUser(editorId)).willReturn(true);

                Place existingPlace = Place.createOf(
                                "기존 장소",
                                Address.of("서울시 종로구 묘동 123-45", "서울시 종로구 묘동길 1"),
                                Position.of(Double.valueOf("37.5700"), Double.valueOf("126.9800")),
                                "도보 3분");
                given(placeRepository.findByPosition(any(Position.class))).willReturn(Optional.of(existingPlace));

                PostPlace postPlace = PostPlace.createOf(savedPost, existingPlace,
                                placeInfoRequest.getDescription(), null, editorId);
                given(postPlacesRepository.save(any(PostPlace.class))).willReturn(postPlace);

                // when
                PostCommandDto.Response response = postCommandService.createPost(request, editorId);

                // then
                assertThat(response.getPlaceInfoResponseList()).hasSize(1);
                assertThat(response.getPlaceInfoResponseList().get(0).getName()).isEqualTo("기존 장소");
                assertThat(response.getPlaceInfoResponseList().get(0).getPhoneNumber()).isEqualTo("02-1234-0000");

                verify(postRepository).save(any(Post.class));
                verify(placeRepository).findByPosition(any(Position.class));
                verify(placeRepository).save(existingPlace);
                verify(postPlacesRepository).save(any(PostPlace.class));
        }

        @Test
        @DisplayName("Post 생성 시 기존 Place 전화번호가 없으면 요청 전화번호로 보강한다")
        void createPost_withExistingPlaceWithoutPhone_updatesPhoneNumber() {
                UUID editorId = UUID.randomUUID();
                PostCommandDto.Request.PlaceInfoRequest placeInfoRequest = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("기존 장소")
                                .description("설명")
                                .addressName("서울시 종로구 묘동 123-45")
                                .roadAddressName("서울시 종로구 묘동길 1")
                                .latitude(Double.valueOf("37.5700"))
                                .longitude(Double.valueOf("126.9800"))
                                .phoneNumber("02-1111-2222")
                                .build();
                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("https://www.instagram.com/post2")
                                .hashTags(List.of("#맛집"))
                                .placeInfoRequestList(List.of(placeInfoRequest))
                                .build();

                Post savedPost = Post.createOf(editorId, request.getUrl(), request.getHashTags());
                Place existingPlace = Place.createOf(
                                "기존 장소",
                                Address.of("서울시 종로구 묘동 123-45", "서울시 종로구 묘동길 1"),
                                Position.of(Double.valueOf("37.5700"), Double.valueOf("126.9800")),
                                "도보 3분");

                given(postRepository.save(any(Post.class))).willReturn(savedPost);
                given(userClient.existsUser(editorId)).willReturn(true);
                given(placeRepository.findByPosition(any(Position.class))).willReturn(Optional.of(existingPlace));
                given(placeRepository.save(any(Place.class))).willAnswer(invocation -> invocation.getArgument(0));
                given(postPlacesRepository.save(any(PostPlace.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                PostCommandDto.Response response = postCommandService.createPost(request, editorId);

                assertThat(response.getPlaceInfoResponseList()).hasSize(1);
                assertThat(response.getPlaceInfoResponseList().get(0).getPhoneNumber()).isEqualTo("02-1111-2222");
                verify(placeRepository).save(existingPlace);
        }

        @Test
        @DisplayName("Post 생성 시 기존 Place 전화번호가 있으면 요청값으로 덮어쓰지 않는다")
        void createPost_withExistingPlaceWithPhone_keepsOriginalPhoneNumber() {
                UUID editorId = UUID.randomUUID();
                PostCommandDto.Request.PlaceInfoRequest placeInfoRequest = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("기존 장소")
                                .description("설명")
                                .addressName("서울시 종로구 묘동 123-45")
                                .roadAddressName("서울시 종로구 묘동길 1")
                                .latitude(Double.valueOf("37.5700"))
                                .longitude(Double.valueOf("126.9800"))
                                .phoneNumber("02-9999-9999")
                                .build();
                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("https://www.instagram.com/post3")
                                .hashTags(List.of("#카페"))
                                .placeInfoRequestList(List.of(placeInfoRequest))
                                .build();

                Post savedPost = Post.createOf(editorId, request.getUrl(), request.getHashTags());
                Place existingPlace = Place.createOf(
                                "기존 장소",
                                Address.of("서울시 종로구 묘동 123-45", "서울시 종로구 묘동길 1"),
                                Position.of(Double.valueOf("37.5700"), Double.valueOf("126.9800")),
                                "도보 3분",
                                null,
                                "02-1234-5678");

                given(postRepository.save(any(Post.class))).willReturn(savedPost);
                given(userClient.existsUser(editorId)).willReturn(true);
                given(placeRepository.findByPosition(any(Position.class))).willReturn(Optional.of(existingPlace));
                given(postPlacesRepository.save(any(PostPlace.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                PostCommandDto.Response response = postCommandService.createPost(request, editorId);

                assertThat(response.getPlaceInfoResponseList()).hasSize(1);
                assertThat(response.getPlaceInfoResponseList().get(0).getPhoneNumber()).isEqualTo("02-1234-5678");
                verify(placeRepository, never()).save(existingPlace);
        }

        @Test
        @DisplayName("여러 Place를 포함한 Post 생성 테스트")
        void createPost_withMultiplePlaces_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                String url = "https://www.instagram.com/post3";
                java.util.List<String> hashTags = java.util.List.of("#여행", "#카페");

                PostCommandDto.Request.PlaceInfoRequest place1 = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("장소1")
                                .addressName("주소1")
                                .roadAddressName("도로명주소1")
                                .description("설명1")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 4분")
                                .build();

                PostCommandDto.Request.PlaceInfoRequest place2 = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("장소2")
                                .addressName("주소2")
                                .roadAddressName("도로명주소2")
                                .description("설명2")
                                .latitude(Double.valueOf("37.5700"))
                                .longitude(Double.valueOf("126.9800"))
                                .nearestStationWalkTime("도보 6분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url(url)
                                .hashTags(hashTags)
                                .placeInfoRequestList(List.of(place1, place2))
                                .build();

                Post savedPost = Post.createOf(editorId, url, hashTags);
                given(postRepository.save(any(Post.class))).willReturn(savedPost);
                given(userClient.existsUser(editorId)).willReturn(true);
                given(placeRepository.findByPosition(any(Position.class))).willReturn(Optional.empty());
                given(placeRepository.save(any(Place.class))).willAnswer(invocation -> invocation.getArgument(0));
                given(postPlacesRepository.save(any(PostPlace.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                // when
                PostCommandDto.Response response = postCommandService.createPost(request, editorId);

                // then
                assertThat(response.getPlaceInfoResponseList()).hasSize(2);

                verify(postRepository).save(any(Post.class));
                verify(placeRepository, times(2)).findByPosition(any(Position.class));
                verify(placeRepository, times(2)).save(any(Place.class));
                verify(postPlacesRepository, times(2)).save(any(PostPlace.class));
        }

        @Test
        @DisplayName("게시글 삭제 성공")
        void deletePost_success() {
                UUID editorId = UUID.randomUUID();
                Long postId = 10L;
                Post post = Post.builder()
                                .id(postId)
                                .editorId(editorId)
                                .url(InstagramUrl.from("https://www.instagram.com/p/delete1"))
                                .hashTags(HashTags.from(List.of("#삭제")))
                                .isDeleted(false)
                                .build();

                PostPlace postPlace1 = PostPlace.builder()
                                .id(1L)
                                .post(post)
                                .place(Place.builder().id(100L).build())
                                .editorId(editorId)
                                .build();
                PostPlace postPlace2 = PostPlace.builder()
                                .id(2L)
                                .post(post)
                                .place(Place.builder().id(200L).build())
                                .editorId(editorId)
                                .build();

                given(postRepository.findById(postId)).willReturn(Optional.of(post));
                given(postPlacesRepository.findAllByPostId(postId)).willReturn(List.of(postPlace1, postPlace2));

                postCommandService.deletePost(postId, editorId);

                assertThat(post.isDeleted()).isTrue();
                assertThat(post.getDeletedAt()).isNotNull();
                verify(postRepository).save(post);
                verify(postPlacesRepository).markDeletedAllByPostId(eq(postId), eq(editorId), any(java.time.LocalDateTime.class));
                verify(postOutboxService).appendPostDeletedEvent(post, List.of(100L, 200L));
        }

        @Test
        @DisplayName("이미 삭제된 게시글 삭제는 멱등 처리")
        void deletePost_idempotentWhenAlreadyDeleted() {
                UUID editorId = UUID.randomUUID();
                Long postId = 11L;
                Post deletedPost = Post.builder()
                                .id(postId)
                                .editorId(editorId)
                                .url(InstagramUrl.from("https://www.instagram.com/p/delete2"))
                                .hashTags(HashTags.from(List.of("#삭제")))
                                .isDeleted(true)
                                .build();

                given(postRepository.findById(postId)).willReturn(Optional.of(deletedPost));

                postCommandService.deletePost(postId, editorId);

                verify(postRepository, never()).save(any(Post.class));
                verify(postPlacesRepository, never()).findAllByPostId(any(Long.class));
                verify(postPlacesRepository, never()).markDeletedAllByPostId(any(Long.class), any(UUID.class),
                                any(java.time.LocalDateTime.class));
                verify(postOutboxService, never()).appendPostDeletedEvent(any(Post.class), anyList());
        }

        @Test
        @DisplayName("게시글 삭제 실패 - 존재하지 않음")
        void deletePost_notFound() {
                UUID editorId = UUID.randomUUID();
                Long postId = 12L;
                given(postRepository.findById(postId)).willReturn(Optional.empty());

                assertThatThrownBy(() -> postCommandService.deletePost(postId, editorId))
                                .isInstanceOf(zero.conflict.archiview.global.error.DomainException.class)
                                .extracting(ex -> ((zero.conflict.archiview.global.error.DomainException) ex).getErrorCode())
                                .isEqualTo(zero.conflict.archiview.post.domain.error.PostErrorCode.POST_NOT_FOUND);
        }

        @Test
        @DisplayName("게시글 삭제 실패 - 권한 없음")
        void deletePost_forbidden() {
                UUID ownerId = UUID.randomUUID();
                UUID otherEditorId = UUID.randomUUID();
                Long postId = 13L;
                Post post = Post.builder()
                                .id(postId)
                                .editorId(ownerId)
                                .url(InstagramUrl.from("https://www.instagram.com/p/delete3"))
                                .hashTags(HashTags.from(List.of("#삭제")))
                                .isDeleted(false)
                                .build();
                given(postRepository.findById(postId)).willReturn(Optional.of(post));

                assertThatThrownBy(() -> postCommandService.deletePost(postId, otherEditorId))
                                .isInstanceOf(zero.conflict.archiview.global.error.DomainException.class)
                                .extracting(ex -> ((zero.conflict.archiview.global.error.DomainException) ex).getErrorCode())
                                .isEqualTo(zero.conflict.archiview.post.domain.error.PostErrorCode.POST_FORBIDDEN);

                verify(postPlacesRepository, never()).markDeletedAllByPostId(any(Long.class), any(UUID.class),
                                any(java.time.LocalDateTime.class));
                verify(postOutboxService, never()).appendPostDeletedEvent(any(Post.class), anyList());
        }
}
