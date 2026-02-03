package zero.conflict.archiview.post.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.post.application.command.PostCommandService;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        private zero.conflict.archiview.user.application.port.UserRepository userRepository;

        @Mock
        private zero.conflict.archiview.global.infra.s3.S3Service s3Service;

        @Test
        @DisplayName("Post 생성 시 Place가 새로 생성되어야 한다")
        void createPost_withNewPlace_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                String url = "https://www.instagram.com/post1";
                String hashTag = "여행";

                PostCommandDto.Request.PlaceInfoRequest placeInfoRequest = PostCommandDto.Request.PlaceInfoRequest
                                .builder()
                                .name("테스트 장소")
                                .roadAddress("서울시 강남구")
                                .detailAddress("101호")
                                .zipCode("12345")
                                .description("멋진 장소")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 5분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url(url)
                                .hashTag(hashTag)
                                .placeInfoRequestList(List.of(placeInfoRequest))
                                .build();

                Post savedPost = Post.createOf(editorId, url, hashTag);
                given(postRepository.save(any(Post.class))).willReturn(savedPost);

                Place newPlace = Place.createOf(
                                placeInfoRequest.getName(),
                                Address.of(placeInfoRequest.getRoadAddress(), placeInfoRequest.getDetailAddress(),
                                                placeInfoRequest.getZipCode()),
                                Position.of(placeInfoRequest.getLatitude(), placeInfoRequest.getLongitude()),
                                placeInfoRequest.getNearestStationWalkTime());
                given(placeRepository.findByPosition(any(Position.class))).willReturn(Optional.empty());
                given(placeRepository.save(any(Place.class))).willReturn(newPlace);

                zero.conflict.archiview.user.domain.User editor = zero.conflict.archiview.user.domain.User.builder()
                                .id(editorId).build();
                given(userRepository.findById(editorId)).willReturn(Optional.of(editor));

                PostPlace postPlace = PostPlace.createOf(savedPost, newPlace,
                                placeInfoRequest.getDescription(),
                                null, editorId);
                given(postPlacesRepository.save(any(PostPlace.class))).willReturn(postPlace);

                // when
                PostCommandDto.Response response = postCommandService.createPost(request, editorId);

                // then
                assertThat(response.getUrl()).isEqualTo(url);
                assertThat(response.getHashTag()).isEqualTo(hashTag);
                assertThat(response.getPlaceInfoResponseList()).hasSize(1);
                assertThat(response.getPlaceInfoResponseList().get(0).getName()).isEqualTo("테스트 장소");

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
                String hashTag = "맛집";

                PostCommandDto.Request.PlaceInfoRequest placeInfoRequest = PostCommandDto.Request.PlaceInfoRequest
                                .builder()
                                .name("기존 장소")
                                .roadAddress("서울시 종로구")
                                .detailAddress("201호")
                                .zipCode("54321")
                                .description("이미 존재하는 장소")
                                .latitude(Double.valueOf("37.5700"))
                                .longitude(Double.valueOf("126.9800"))
                                .nearestStationWalkTime("도보 3분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url(url)
                                .hashTag(hashTag)
                                .placeInfoRequestList(List.of(placeInfoRequest))
                                .build();

                Post savedPost = Post.createOf(editorId, url, hashTag);
                given(postRepository.save(any(Post.class))).willReturn(savedPost);

                zero.conflict.archiview.user.domain.User editor = zero.conflict.archiview.user.domain.User.builder()
                                .id(editorId).build();
                given(userRepository.findById(editorId)).willReturn(Optional.of(editor));

                Place existingPlace = Place.createOf(
                                "기존 장소",
                                Address.of("서울시 종로구", "201호", "54321"),
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

                verify(postRepository).save(any(Post.class));
                verify(placeRepository).findByPosition(any(Position.class));
                verify(placeRepository, never()).save(any(Place.class)); // 새로운 Place는 저장되지 않아야 함
                verify(postPlacesRepository).save(any(PostPlace.class));
        }

        @Test
        @DisplayName("여러 Place를 포함한 Post 생성 테스트")
        void createPost_withMultiplePlaces_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                String url = "https://www.instagram.com/post3";
                String hashTag = "여행";

                PostCommandDto.Request.PlaceInfoRequest place1 = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .name("장소1")
                                .roadAddress("주소1")
                                .detailAddress("상세주소1")
                                .zipCode("11111")
                                .description("설명1")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 4분")
                                .build();

                PostCommandDto.Request.PlaceInfoRequest place2 = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .name("장소2")
                                .roadAddress("주소2")
                                .detailAddress("상세주소2")
                                .zipCode("22222")
                                .description("설명2")
                                .latitude(Double.valueOf("37.5700"))
                                .longitude(Double.valueOf("126.9800"))
                                .nearestStationWalkTime("도보 6분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url(url)
                                .hashTag(hashTag)
                                .placeInfoRequestList(List.of(place1, place2))
                                .build();

                Post savedPost = Post.createOf(editorId, url, hashTag);
                given(postRepository.save(any(Post.class))).willReturn(savedPost);
                zero.conflict.archiview.user.domain.User editor = zero.conflict.archiview.user.domain.User.builder()
                                .id(editorId).build();
                given(userRepository.findById(editorId)).willReturn(Optional.of(editor));
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
}
