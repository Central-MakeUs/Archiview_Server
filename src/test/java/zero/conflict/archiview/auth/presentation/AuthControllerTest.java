package zero.conflict.archiview.auth.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.auth.application.MobileAuthService;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends ControllerTestSupport {

    @MockBean
    private MobileAuthService mobileAuthService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("현재 사용자 조회 - 성공")
    void getCurrentUser_success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void refreshToken_success() throws Exception {
        String refreshToken = "refresh-token";
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("testuser")
                .provider(User.OAuthProvider.GOOGLE)
                .providerId("google-test")
                .role(User.Role.EDITOR)
                .build();

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(any())).willReturn("new-access-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
    void refreshToken_invalidToken() throws Exception {
        given(jwtTokenProvider.validateToken("bad-token")).willReturn(false);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "bad-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - refreshToken 누락")
    void refreshToken_missingToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("모바일 카카오 로그인 성공")
    void mobileKakaoLogin_success() throws Exception {
        Map<String, Object> response = Map.of(
                "accessToken", "access-token",
                "refreshToken", "refresh-token",
                "userId", UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "email", "test@example.com",
                "name", "testuser"
        );
        given(mobileAuthService.loginWithKakao(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/mobile/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "kakao-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("모바일 애플 로그인 성공")
    void mobileAppleLogin_success() throws Exception {
        Map<String, Object> response = Map.of(
                "accessToken", "access-token",
                "refreshToken", "refresh-token",
                "userId", UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "email", "test@example.com",
                "name", "testuser"
        );
        given(mobileAuthService.loginWithApple(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/mobile/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", "apple-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("모바일 로그인 실패 - idToken 누락")
    void mobileLogin_fail_missingIdToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/mobile/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idToken", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("모바일 로그인 실패 - role 값 오류")
    void mobileLogin_fail_invalidRole() throws Exception {
        mockMvc.perform(post("/api/v1/auth/mobile/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idToken", "kakao-id-token",
                                "role", "ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));
    }
}
