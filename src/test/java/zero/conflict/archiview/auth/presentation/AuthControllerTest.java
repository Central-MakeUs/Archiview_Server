package zero.conflict.archiview.auth.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.auth.application.MobileAuthService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends ControllerTestSupport {

    @MockBean
    private MobileAuthService mobileAuthService;

    @Test
    @DisplayName("모바일 카카오 로그인 성공")
    void mobileKakaoLogin_success() throws Exception {
        // given
        Map<String, Object> response = Map.of(
                "accessToken", "access-token",
                "refreshToken", "refresh-token",
                "userId", java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "email", "test@example.com",
                "name", "testuser"
        );
        given(mobileAuthService.loginWithKakao(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/mobile/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "kakao-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andDo(print());
    }

    @Test
    @DisplayName("모바일 애플 로그인 성공")
    void mobileAppleLogin_success() throws Exception {
        // given
        Map<String, Object> response = Map.of(
                "accessToken", "access-token",
                "refreshToken", "refresh-token",
                "userId", java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "email", "test@example.com",
                "name", "testuser"
        );
        given(mobileAuthService.loginWithApple(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/mobile/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "apple-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andDo(print());
    }

    @Test
    @DisplayName("모바일 로그인 실패 - idToken 누락")
    void mobileLogin_fail_missingIdToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/mobile/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", ""))))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
