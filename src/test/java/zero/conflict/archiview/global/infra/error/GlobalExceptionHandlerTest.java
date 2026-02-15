package zero.conflict.archiview.global.infra.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import zero.conflict.archiview.ControllerTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @DisplayName("허용되지 않은 메서드 요청 시 405와 METHOD_NOT_ALLOWED를 반환한다")
    void methodNotAllowed_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/v1/archivers/follows")
                        .with(authenticatedUser()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("요청하신 경로에서 허용되지 않는 메서드입니다."));
    }
}
