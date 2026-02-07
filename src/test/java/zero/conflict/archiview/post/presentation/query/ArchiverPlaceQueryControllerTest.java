package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverPlaceQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private PostQueryService postQueryService;

    @Test
    @DisplayName("내 주변 1km 장소 조회 - 성공")
    void getNearbyPlaces_success() throws Exception {
        CategoryQueryDto.CategoryPlaceResponse place = CategoryQueryDto.CategoryPlaceResponse.builder()
                .placeId(1L)
                .placeName("성수 감성 카페")
                .latestDescription("최근 설명")
                .viewCount(200L)
                .saveCount(45L)
                .build();
        CategoryQueryDto.CategoryPlaceListResponse response = CategoryQueryDto.CategoryPlaceListResponse.builder()
                .totalCount(1L)
                .places(List.of(place))
                .build();
        given(postQueryService.getNearbyPlacesWithin1km(37.5445, 127.0560)).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/places/nearby")
                        .queryParam("latitude", "37.5445")
                .queryParam("longitude", "127.0560"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.places[0].placeName").value("성수 감성 카페"))
                .andExpect(jsonPath("$.data.places[0].saveCount").value(45));
    }

    @Test
    @DisplayName("내 주변 1km 장소 조회 - mock")
    void getNearbyPlaces_mock() throws Exception {
        mockMvc.perform(get("/api/v1/archivers/places/nearby")
                        .queryParam("latitude", "37.5445")
                        .queryParam("longitude", "127.0560")
                .queryParam("useMock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.places").isArray());
    }
}
