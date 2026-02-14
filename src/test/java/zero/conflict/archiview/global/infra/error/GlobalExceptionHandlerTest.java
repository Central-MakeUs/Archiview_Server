package zero.conflict.archiview.global.infra.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import zero.conflict.archiview.ControllerTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest extends ControllerTestSupport {

    @Test
    @DisplayName("없는 API 요청 시 404와 API_NOT_FOUND를 반환한다")
    void unknownApi_returnsApiNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/not-existing-endpoint")
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("API_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("요청하신 API를 찾을 수 없습니다."));
    }
}

