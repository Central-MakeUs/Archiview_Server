package zero.conflict.archiview.post.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.infrastructure.CategoryPlaceReadRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryQueryService 테스트")
class CategoryQueryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryPlaceReadRepository categoryPlaceReadRepository;

    @Mock
    private CategoryPlaceReadRepository.CategoryPlaceSummaryProjection projection;

    @Test
    @DisplayName("카테고리별 장소 목록을 조회한다")
    void getPlacesByCategoryId_success() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder().id(categoryId).name("카페").build();
        CategoryQueryService service = new CategoryQueryService(categoryRepository, categoryPlaceReadRepository);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryPlaceReadRepository.findPlaceSummariesByCategoryId(categoryId)).willReturn(List.of(projection));
        given(projection.getPlaceId()).willReturn(10L);
        given(projection.getPlaceName()).willReturn("성수 카페");
        given(projection.getLatestDescription()).willReturn("최근 설명");
        given(projection.getViewCount()).willReturn(120L);
        given(projection.getSaveCount()).willReturn(35L);

        // when
        CategoryQueryDto.CategoryPlaceListResponse response = service.getPlacesByCategoryId(categoryId);

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
        CategoryQueryService service = new CategoryQueryService(categoryRepository, categoryPlaceReadRepository);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getPlacesByCategoryId(categoryId))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", PostErrorCode.INVALID_CATEGORY_ID);
    }
}
