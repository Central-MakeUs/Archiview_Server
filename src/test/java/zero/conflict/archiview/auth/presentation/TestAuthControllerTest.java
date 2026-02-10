package zero.conflict.archiview.auth.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TestAuthControllerTest extends ControllerTestSupport {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("테스트용 에디터 로그인 토큰 발급 - 성공")
    void getEditorTestToken_success() throws Exception {
        User user = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000301"))
                .email("test-editor@archiview.com")
                .name("테스트 에디터")
                .provider(User.OAuthProvider.GOOGLE)
                .providerId("test-editor@archiview.com")
                .role(User.Role.EDITOR)
                .build();

        given(userRepository.findByProviderAndProviderId(eq(User.OAuthProvider.GOOGLE), eq("test-editor@archiview.com")))
                .willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(any(), eq(1000L * 60 * 60 * 24 * 30))).willReturn("editor-access");
        given(jwtTokenProvider.createRefreshToken(user.getId())).willReturn("editor-refresh");

        mockMvc.perform(get("/api/v1/auth/test/editor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("editor-access"))
                .andExpect(jsonPath("$.data.role").value("EDITOR"));
    }

    @Test
    @DisplayName("테스트용 아카이버 로그인 토큰 발급 - 성공")
    void getArchiverTestToken_success() throws Exception {
        User user = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000302"))
                .email("test-archiver@archiview.com")
                .name("테스트 아카이버")
                .provider(User.OAuthProvider.GOOGLE)
                .providerId("test-archiver@archiview.com")
                .role(User.Role.ARCHIVER)
                .build();

        given(userRepository.findByProviderAndProviderId(eq(User.OAuthProvider.GOOGLE), eq("test-archiver@archiview.com")))
                .willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(any(), eq(1000L * 60 * 60 * 24 * 30))).willReturn("archiver-access");
        given(jwtTokenProvider.createRefreshToken(user.getId())).willReturn("archiver-refresh");

        mockMvc.perform(get("/api/v1/auth/test/archiver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("archiver-access"))
                .andExpect(jsonPath("$.data.role").value("ARCHIVER"));
    }
}
