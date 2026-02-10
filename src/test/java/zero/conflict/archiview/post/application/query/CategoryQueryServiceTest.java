package zero.conflict.archiview.post.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.application.query.ArchiverVisibilityService.VisibilityFilter;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryQueryService 테스트")
class CategoryQueryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostPlaceRepository postPlaceRepository;

    @Mock
    private ArchiverVisibilityService archiverVisibilityService;

    @Test
    @DisplayName("카테고리별 장소 목록을 조회한다")
    void getPlacesByCategoryId_success() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder().id(categoryId).name("카페").build();
        CategoryQueryService service = new CategoryQueryService(
                categoryRepository,
                postPlaceRepository,
                archiverVisibilityService);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        UUID editorId = UUID.randomUUID();
        Post post = Post.builder().id(1L).editorId(editorId).build();
        Place place = Place.builder().id(10L).name("성수 카페").build();
        PostPlace postPlace = PostPlace.builder()
                .id(100L)
                .post(post)
                .place(place)
                .editorId(editorId)
                .description("최근 설명")
                .viewCount(120L)
                .saveCount(35L)
                .build();
        given(postPlaceRepository.findAllByCategoryId(categoryId)).willReturn(List.of(postPlace));
        VisibilityFilter visibilityFilter = new VisibilityFilter(Set.of(), Set.of());
        given(archiverVisibilityService.getVisibilityFilter(editorId))
                .willReturn(visibilityFilter);
        given(archiverVisibilityService.filterVisiblePostPlaces(any(), any()))
                .willReturn(List.of(postPlace));

        // when
        CategoryQueryDto.CategoryPlaceListResponse response = service.getPlacesByCategoryId(categoryId, editorId);

        // then
        assertThat(response.getPlaces()).hasSize(1);
        assertThat(response.getTotalCount()).isEqualTo(1L);
        assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("성수 카페");
        assertThat(response.getPlaces().get(0).getLatestDescription()).isEqualTo("최근 설명");
        assertThat(response.getPlaces().get(0).getViewCount()).isEqualTo(120L);
        assertThat(response.getPlaces().get(0).getSaveCount()).isEqualTo(35L);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리면 예외가 발생한다")
    void getPlacesByCategoryId_invalidCategory() {
        // given
        Long categoryId = 999L;
        CategoryQueryService service = new CategoryQueryService(
                categoryRepository,
                postPlaceRepository,
                archiverVisibilityService);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getPlacesByCategoryId(categoryId))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", PostErrorCode.INVALID_CATEGORY_ID);
    }
}
