package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.archiver.query.CategoryQueryService;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private CategoryQueryService categoryQueryService;

    @Test
    @DisplayName("카테고리 목록 조회 - 성공")
    void getCategories_success() throws Exception {
        CategoryQueryDto.CategoryResponse category = CategoryQueryDto.CategoryResponse.builder()
                .id(1L)
                .name("카페")
                .build();
        CategoryQueryDto.CategoryListResponse response = CategoryQueryDto.CategoryListResponse.of(List.of(category));

        given(categoryQueryService.getCategories()).willReturn(response);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories[0].id").value(1L))
                .andExpect(jsonPath("$.data.categories[0].name").value("카페"));
    }

    @Test
    @DisplayName("카테고리별 장소 목록 조회 - 성공")
    void getPlacesByCategoryId_success() throws Exception {
        Long categoryId = 1L;
        CategoryQueryDto.CategoryPlaceResponse place = CategoryQueryDto.CategoryPlaceResponse.builder()
                .placeId(100L)
                .placeName("성수 핫플")
                .latestDescription("가장 최근 설명")
                .imageUrl("https://example.com/hotplace-100.jpg")
                .viewCount(1200L)
                .saveCount(340L)
                .build();
        CategoryQueryDto.CategoryPlaceListResponse response = CategoryQueryDto.CategoryPlaceListResponse.builder()
                .totalCount(1L)
                .places(List.of(place))
                .build();

        given(categoryQueryService.getPlacesByCategoryId(eq(categoryId), any(UUID.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/categories/{categoryId}/places", categoryId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.places[0].placeName").value("성수 핫플"))
                .andExpect(jsonPath("$.data.places[0].imageUrl").value("https://example.com/hotplace-100.jpg"));
    }

    @Test
    @DisplayName("카테고리별 장소 목록 조회 - 목데이터")
    void getPlacesByCategoryId_mock() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}/places", 1L)
                        .queryParam("useMock", "true")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.places").isArray())
                .andExpect(jsonPath("$.data.places[0].placeName").value("성수 감성 카페"))
                .andExpect(jsonPath("$.data.places[0].imageUrl").value("https://example.com/images/cafe-101.jpg"));
    }
}
