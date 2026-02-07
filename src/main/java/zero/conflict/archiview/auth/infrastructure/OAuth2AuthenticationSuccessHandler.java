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

    @org.springframework.beans.factory.annotation.Value("${auth.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(oAuth2User);
        String refreshToken = jwtTokenProvider.createRefreshToken(oAuth2User.getUserId());

        log.info("OAuth2 로그인 성공 - 사용자 ID: {}, 이메일: {}, Role: {}",
                oAuth2User.getUserId(), oAuth2User.getUsername(), oAuth2User.getUser().getRole());

        String targetPath = switch (oAuth2User.getUser().getRole()) {
            case EDITOR -> "/editor/home";
            case ARCHIVER -> "/archiver/home";
            case GUEST -> "/login";
        };

        String targetUrl = org.springframework.web.util.UriComponentsBuilder.fromUriString(frontendUrl + targetPath)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
