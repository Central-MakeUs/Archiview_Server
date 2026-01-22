package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.query.PostQueryService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditorPostQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private PostQueryService postQueryService;

    @Test
    @DisplayName("에디터 인사이트 장소 목록 조회 - 기본 정렬 및 기간 필터 없음")
    void getInsightPlaces_DefaultSort_NoPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/editors/me/insights/places")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sort").value("RECENT"))
                .andExpect(jsonPath("$.data.period").doesNotExist())
                .andExpect(jsonPath("$.data.places").isArray())
                .andExpect(jsonPath("$.data.places").isEmpty());
    }
}
