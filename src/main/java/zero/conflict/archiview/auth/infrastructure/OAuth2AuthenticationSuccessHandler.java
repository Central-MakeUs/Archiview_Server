package zero.conflict.archiview.auth.infrastructure;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.infrastructure.persistence.DevLoginRedirectAllowlistJpaRepository;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final DevLoginRedirectAllowlistJpaRepository devLoginRedirectAllowlistJpaRepository;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @org.springframework.beans.factory.annotation.Value("${auth.frontend-url:https://archiview.space/}")
    private String frontendUrl;
    @org.springframework.beans.factory.annotation.Value("${auth.dev-frontend-url:http://localhost:3000/}")
    private String devFrontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(oAuth2User);
        String refreshToken = jwtTokenProvider.createRefreshToken(oAuth2User.getUserId());

        log.info("OAuth2 로그인 성공 - 사용자 ID: {}, 이메일: {}, Role: {}",
                oAuth2User.getUserId(), oAuth2User.getUsername(), oAuth2User.getUser().getRole());

        String targetPath = switch (oAuth2User.getUser().getRole()) {
            case EDITOR -> "editor/home/";
            case ARCHIVER -> "archiver/home/";
            case GUEST -> "term-agree/";
        };

        OAuth2AuthorizationRequest authorizationRequest = authorizationRequestRepository.removeAuthorizationRequest(request,
                response);
        boolean devRequested = authorizationRequest != null
                && Boolean.TRUE.equals(authorizationRequest.getAttributes().get("dev"));
        boolean devAllowed = isDevAllowedEmail(oAuth2User.getUsername());
        String resolvedFrontendUrl = (devRequested && devAllowed) ? devFrontendUrl : frontendUrl;

        log.info("OAuth2 로그인 리다이렉트 대상 결정 - devRequested: {}, devAllowed: {}, frontendUrl: {}",
                devRequested, devAllowed, resolvedFrontendUrl);

        String targetUrl = org.springframework.web.util.UriComponentsBuilder.fromUriString(resolvedFrontendUrl)
                .path(targetPath)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isDevAllowedEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return devLoginRedirectAllowlistJpaRepository.existsByEmailIgnoreCaseAndEnabledTrue(email);
    }
}
