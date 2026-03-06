package zero.conflict.archiview.auth.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AuthenticationFailureHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler(objectMapper);

    @Test
    @DisplayName("authorization_request_not_found 실패 시 모바일 API 가이드를 응답한다")
    void onAuthenticationFailure_authorizationRequestNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/kakao");
        request.setParameter("state", "state-value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("authorization_request_not_found"),
                "authorization request not found");

        handler.onAuthenticationFailure(request, response, exception);

        Map<String, Object> body = objectMapper.readValue(response.getContentAsByteArray(), new TypeReference<>() {
        });
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.get("message")).asString().contains("/api/v1/auth/mobile/kakao");
        assertThat(body.get("hint")).asString().contains("카카오 SDK");
    }

    @Test
    @DisplayName("기타 인증 실패 시 원래 예외 메시지를 유지한다")
    void onAuthenticationFailure_otherException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/kakao");
        MockHttpServletResponse response = new MockHttpServletResponse();

        BadCredentialsException exception = new BadCredentialsException("bad credentials");

        handler.onAuthenticationFailure(request, response, exception);

        Map<String, Object> body = objectMapper.readValue(response.getContentAsByteArray(), new TypeReference<>() {
        });
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.get("message")).isEqualTo("bad credentials");
        assertThat(body).doesNotContainKey("hint");
    }
}
