package zero.conflict.archiview.post.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.*;
import zero.conflict.archiview.post.presentation.query.dto.EditorMapDto;
import zero.conflict.archiview.post.presentation.query.dto.EditorMapDto.MapFilter;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostQueryService 테스트")
class PostQueryServiceTest {

        @InjectMocks
        private PostQueryService postQueryService;

        @Mock
        private PostPlaceRepository postPlaceRepository;

        @Mock
        private PlaceRepository placeRepository;

        @Mock
        private PostRepository postRepository;

        @Mock
        private EditorProfileRepository editorProfileRepository;

        @Test
        @DisplayName("에디터의 모든 핀을 조회한다")
        void getMapPins_all_success() {
                // given
                java.util.UUID editorId = java.util.UUID.randomUUID();
                Category category = Category.builder().id(java.util.UUID.randomUUID()).name("한식").build();

                Place place = Place.builder()
                                .id(java.util.UUID.randomUUID())
                                .name("식당")
                                .position(Position.of(37.0, 127.0))
                                .build();

                Post post = Post.builder().id(java.util.UUID.randomUUID()).build();

                PostPlace postPlace = PostPlace.builder()
                                .id(java.util.UUID.randomUUID())
                                .post(post)
                                .place(place)
                                .editorId(editorId)
                                .build();
                postPlace.addCategory(category);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(postPlace));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place));

                // when
                EditorMapDto.Response response = postQueryService.getMapPins(
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
                Category category1 = Category.builder().id(java.util.UUID.randomUUID()).name("한식").build();
                Category category2 = Category.builder().id(java.util.UUID.randomUUID()).name("양식").build();

                Place place1 = Place.builder().id(java.util.UUID.randomUUID()).name("한식당").position(Position.of(37.0, 127.0)).build();
                Place place2 = Place.builder().id(java.util.UUID.randomUUID()).name("양식당").position(Position.of(37.1, 127.1)).build();

                Post post = Post.builder().id(java.util.UUID.randomUUID()).build();

                PostPlace pp1 = PostPlace.builder().id(java.util.UUID.randomUUID()).post(post).place(place1).editorId(editorId).build();
                pp1.addCategory(category1);

                PostPlace pp2 = PostPlace.builder().id(java.util.UUID.randomUUID()).post(post).place(place2).editorId(editorId).build();
                pp2.addCategory(category2);

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(place1));

                // when (Filter by category1)
                EditorMapDto.Response response = postQueryService.getMapPins(
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
                Place nearPlace = Place.builder().id(java.util.UUID.randomUUID()).name("가까운곳").position(Position.of(37.0001, 127.0001)).build();
                Place farPlace = Place.builder().id(java.util.UUID.randomUUID()).name("먼곳").position(Position.of(37.5, 127.5)).build();

                Post post = Post.builder().id(java.util.UUID.randomUUID()).build();

                PostPlace pp1 = PostPlace.builder().id(java.util.UUID.randomUUID()).post(post).place(nearPlace).editorId(editorId).build();
                PostPlace pp2 = PostPlace.builder().id(java.util.UUID.randomUUID()).post(post).place(farPlace).editorId(editorId).build();

                given(postPlaceRepository.findAllByEditorId(editorId)).willReturn(List.of(pp1, pp2));
                given(placeRepository.findAllByIds(anyList())).willReturn(List.of(nearPlace, farPlace));

                // when (Nearby filter now returns all pins as BBox is removed)
                EditorMapDto.Response response = postQueryService.getMapPins(
                                editorId,
                                MapFilter.NEARBY,
                                null);

                // then
                assertThat(response.getPins()).hasSize(2); // Both near and far pins returned
                assertThat(response.getPins()).extracting("name")
                                .containsExactlyInAnyOrder("가까운곳", "먼곳");
        }
}
