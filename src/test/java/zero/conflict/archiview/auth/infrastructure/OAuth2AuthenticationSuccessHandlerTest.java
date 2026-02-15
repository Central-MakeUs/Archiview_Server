package zero.conflict.archiview.auth.infrastructure;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.domain.DevLoginRedirectAllowlist;
import zero.conflict.archiview.auth.infrastructure.persistence.DevLoginRedirectAllowlistJpaRepository;
import zero.conflict.archiview.user.domain.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    private static final String DEV_LOGIN_STATE_SESSION_KEY = "OAUTH2_DEV_LOGIN_STATE_MAP";

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private DevLoginRedirectAllowlistJpaRepository allowlistRepository;

    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(jwtTokenProvider, allowlistRepository);
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://archiview.space/");
        ReflectionTestUtils.setField(handler, "devFrontendUrl", "http://localhost:3000/");
    }

    @Test
    @DisplayName("dev 로그인 + allowlist 사용자의 redirect_url이 있으면 해당 URL로 리다이렉트한다")
    void redirectToUserSpecificDevUrlWhenPresent() throws ServletException, IOException {
        Authentication authentication = authenticationOf("dev-user@archiview.com", User.Role.EDITOR);
        MockHttpServletRequest request = devRequestWithState("state-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.createAccessToken(org.mockito.ArgumentMatchers.any(CustomOAuth2User.class)))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(org.mockito.ArgumentMatchers.any(UUID.class)))
                .willReturn("refresh-token");
        given(allowlistRepository.existsByEmailIgnoreCaseAndEnabledTrue("dev-user@archiview.com")).willReturn(true);
        given(allowlistRepository.findByEmailIgnoreCaseAndEnabledTrue("dev-user@archiview.com"))
                .willReturn(Optional.of(DevLoginRedirectAllowlist.builder()
                        .email("dev-user@archiview.com")
                        .enabled(true)
                        .redirectUrl("http://192.168.0.6:3000/")
                        .build()));

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl())
                .startsWith("http://192.168.0.6:3000/editor/home/?accessToken=access-token&refreshToken=refresh-token");
    }

    @Test
    @DisplayName("dev 로그인 + redirect_url이 잘못되었으면 기본 devFrontendUrl로 폴백한다")
    void fallbackToDefaultDevFrontendUrlWhenRedirectUrlInvalid() throws ServletException, IOException {
        Authentication authentication = authenticationOf("dev-user@archiview.com", User.Role.EDITOR);
        MockHttpServletRequest request = devRequestWithState("state-2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.createAccessToken(org.mockito.ArgumentMatchers.any(CustomOAuth2User.class)))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(org.mockito.ArgumentMatchers.any(UUID.class)))
                .willReturn("refresh-token");
        given(allowlistRepository.existsByEmailIgnoreCaseAndEnabledTrue("dev-user@archiview.com")).willReturn(true);
        given(allowlistRepository.findByEmailIgnoreCaseAndEnabledTrue("dev-user@archiview.com"))
                .willReturn(Optional.of(DevLoginRedirectAllowlist.builder()
                        .email("dev-user@archiview.com")
                        .enabled(true)
                        .redirectUrl("javascript:alert(1)")
                        .build()));

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl())
                .startsWith("http://localhost:3000/editor/home/?accessToken=access-token&refreshToken=refresh-token");
    }

    @Test
    @DisplayName("dev 요청이 아니면 allowlist 사용자여도 운영 frontendUrl로 리다이렉트한다")
    void redirectToFrontendWhenNotDevRequest() throws ServletException, IOException {
        Authentication authentication = authenticationOf("dev-user@archiview.com", User.Role.ARCHIVER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.createAccessToken(org.mockito.ArgumentMatchers.any(CustomOAuth2User.class)))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(org.mockito.ArgumentMatchers.any(UUID.class)))
                .willReturn("refresh-token");
        given(allowlistRepository.existsByEmailIgnoreCaseAndEnabledTrue("dev-user@archiview.com")).willReturn(true);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl())
                .startsWith("https://archiview.space/archiver/home/?accessToken=access-token&refreshToken=refresh-token");
    }

    private Authentication authenticationOf(String email, User.Role role) {
        User user = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000777"))
                .email(email)
                .name("테스트 사용자")
                .provider(User.OAuthProvider.GOOGLE)
                .providerId(email)
                .role(role)
                .build();
        CustomOAuth2User principal = new CustomOAuth2User(user, Map.of("email", email));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private MockHttpServletRequest devRequestWithState(String state) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("state", state);
        Map<String, Boolean> stateMap = new HashMap<>();
        stateMap.put(state, true);
        request.getSession(true).setAttribute(DEV_LOGIN_STATE_SESSION_KEY, stateMap);
        return request;
    }
}
