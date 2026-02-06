package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.EditorInsightDto;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class EditorPostQueryControllerTest extends ControllerTestSupport {

        @MockBean
        private PostQueryService postQueryService;

        @Test
        @DisplayName("에디터 인사이트 장소 목록 조회 - 기본 정렬 및 기간 필터 없음")
        void getInsightPlaces_DefaultSort_NoPeriod() throws Exception {
                given(postQueryService.getInsightPlaces(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.eq(EditorInsightDto.PlaceSort.RECENT)))
                                .willReturn(EditorInsightDto.PlaceCardListResponse
                                                .empty(EditorInsightDto.PlaceSort.RECENT));

                mockMvc.perform(get("/api/v1/editors/me/insights/places")
                                .with(authenticatedUser()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.sort").value("RECENT"))
                                .andExpect(jsonPath("$.data.period").doesNotExist())
                                .andExpect(jsonPath("$.data.places").isArray())
                                .andExpect(jsonPath("$.data.places").isEmpty());
        }

        @Test
        @DisplayName("에디터 인사이트 장소 상세 조회 - 성공")
        void getInsightPlaceDetail_Success() throws Exception {
                Long placeId = 1L;
                EditorInsightDto.PostPlaceDetailResponse postPlaceDetail = EditorInsightDto.PostPlaceDetailResponse.of(
                                100L, "에디터", "editor_insta", "https://www.instagram.com/post", List.of("#태그", "#맛집"),
                                "설명", List.of("카테고리"));
                EditorInsightDto.PlaceDetailResponse response = EditorInsightDto.PlaceDetailResponse.of(placeId,
                                List.of(postPlaceDetail));

                given(postQueryService.getInsightPlaceDetail(org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.eq(placeId)))
                                .willReturn(response);

                mockMvc.perform(get("/api/v1/editors/me/places/{placeId}", placeId)
                                .with(authenticatedUser()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.placeId").value(placeId))
                                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(100L))
                                .andExpect(jsonPath("$.data.postPlaces[0].editorName").value("에디터"));
        }
}
