package zero.conflict.archiview.auth.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(oAuth2User);
        String refreshToken = jwtTokenProvider.createRefreshToken(oAuth2User.getUserId());

        log.info("OAuth2 로그인 성공 - 사용자 ID: {}, 이메일: {}",
                oAuth2User.getUserId(), oAuth2User.getUsername());

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", accessToken);
        tokenResponse.put("refreshToken", refreshToken);
        tokenResponse.put("userId", oAuth2User.getUserId());
        tokenResponse.put("email", oAuth2User.getUsername());
        tokenResponse.put("name", oAuth2User.getName());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }
}

