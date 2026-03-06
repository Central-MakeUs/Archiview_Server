package zero.conflict.archiview.auth.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String state = request.getParameter("state");
        String sessionId = request.getRequestedSessionId();
        log.error(
                "OAuth2 로그인 실패 - uri: {}, statePresent: {}, requestedSessionIdPresent: {}",
                request.getRequestURI(),
                state != null && !state.isBlank(),
                sessionId != null && !sessionId.isBlank(),
                exception);

        Map<String, Object> errorResponse = new HashMap<>();
        String message = exception.getMessage();
        errorResponse.put("error", "OAuth2 인증 실패");
        if (isAuthorizationRequestNotFound(exception)) {
            message = "모바일 앱 로그인은 웹 OAuth 콜백 대신 /api/v1/auth/mobile/kakao 엔드포인트를 사용하세요.";
            errorResponse.put("hint", "카카오 SDK에서 access token을 획득한 뒤 /api/v1/auth/mobile/kakao 로 POST 요청");
        }
        errorResponse.put("message", message);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private boolean isAuthorizationRequestNotFound(AuthenticationException exception) {
        if (!(exception instanceof OAuth2AuthenticationException oauth2Exception)) {
            return false;
        }
        if (oauth2Exception.getError() == null || oauth2Exception.getError().getErrorCode() == null) {
            return false;
        }
        return "authorization_request_not_found".equals(oauth2Exception.getError().getErrorCode());
    }
}
