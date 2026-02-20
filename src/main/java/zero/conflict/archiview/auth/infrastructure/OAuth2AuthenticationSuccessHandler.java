package zero.conflict.archiview.auth.infrastructure;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.domain.DevLoginRedirectAllowlist;
import zero.conflict.archiview.auth.infrastructure.persistence.DevLoginRedirectAllowlistJpaRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String DEV_LOGIN_STATE_SESSION_KEY = "OAUTH2_DEV_LOGIN_STATE_MAP";
    private static final String LOCALHOST_SOURCE_STATE_SESSION_KEY = "OAUTH2_LOCALHOST_SOURCE_STATE_MAP";

    private final JwtTokenProvider jwtTokenProvider;
    private final DevLoginRedirectAllowlistJpaRepository devLoginRedirectAllowlistJpaRepository;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @org.springframework.beans.factory.annotation.Value("${auth.frontend-url:https://archiview.space/}")
    private String frontendUrl;

    @org.springframework.beans.factory.annotation.Value("${auth.dev-frontend-url:http://localhost:3000/}")
    private String devFrontendUrl;

    @org.springframework.beans.factory.annotation.Value(
            "#{'${auth.local-redirect-allowed-origins:http://localhost:3000,http://127.0.0.1:3000}'.split(',')}")
    private List<String> localRedirectAllowedOrigins;

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
        boolean devRequested = isDevRequestedByState(request);
        boolean localhostSource = isLocalhostSourceByState(request);
        if (!devRequested && authorizationRequest != null) {
            devRequested = Boolean.TRUE.equals(authorizationRequest.getAttributes().get("dev"));
        }
        if (!localhostSource && authorizationRequest != null) {
            localhostSource = Boolean.TRUE.equals(authorizationRequest.getAttributes().get("localhostSource"));
        }
        boolean devAllowed = isDevAllowedEmail(oAuth2User.getUsername());
        String resolvedFrontendUrl = frontendUrl;
        if (localhostSource) {
            resolvedFrontendUrl = devFrontendUrl;
        } else if (devRequested && devAllowed) {
            Optional<String> devRedirectBaseUrl = resolveDevFrontendUrl(oAuth2User.getUsername());
            if (devRedirectBaseUrl.isPresent()) {
                resolvedFrontendUrl = devRedirectBaseUrl.get();
            }
        }

        log.info("OAuth2 로그인 리다이렉트 대상 결정 - localhostSource: {}, devRequested: {}, devAllowed: {}, frontendUrl: {}",
                localhostSource, devRequested, devAllowed, resolvedFrontendUrl);

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

    private Optional<String> resolveDevFrontendUrl(String email) {
        return devLoginRedirectAllowlistJpaRepository.findByEmailIgnoreCaseAndEnabledTrue(email)
                .map(DevLoginRedirectAllowlist::getRedirectUrl)
                .filter(this::isValidRedirectBaseUrl);
    }

    private boolean isValidRedirectBaseUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(redirectUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                return false;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return false;
            }
            if (uri.getUserInfo() != null || uri.getFragment() != null) {
                return false;
            }
            if (isLocalhostHost(uri.getHost())) {
                return isAllowedLocalOrigin(uri);
            }
            return true;
        } catch (URISyntaxException e) {
            log.warn("허용되지 않은 dev redirectUrl 형식 - value: {}", redirectUrl);
            return false;
        }
    }

    private boolean isDevRequestedByState(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null || state.isBlank()) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object stateMapObject = session.getAttribute(DEV_LOGIN_STATE_SESSION_KEY);
        if (!(stateMapObject instanceof Map<?, ?> stateMap)) {
            return false;
        }

        Object requested = stateMap.remove(state);
        if (stateMap.isEmpty()) {
            session.removeAttribute(DEV_LOGIN_STATE_SESSION_KEY);
        }
        return Boolean.TRUE.equals(requested);
    }

    private boolean isLocalhostSourceByState(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null || state.isBlank()) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object stateMapObject = session.getAttribute(LOCALHOST_SOURCE_STATE_SESSION_KEY);
        if (!(stateMapObject instanceof Map<?, ?> stateMap)) {
            return false;
        }

        Object requested = stateMap.remove(state);
        if (stateMap.isEmpty()) {
            session.removeAttribute(LOCALHOST_SOURCE_STATE_SESSION_KEY);
        }
        return Boolean.TRUE.equals(requested);
    }

    private boolean isAllowedLocalOrigin(URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        int effectivePort = uri.getPort() == -1 ? ("https".equals(scheme) ? 443 : 80) : uri.getPort();
        return localRedirectAllowedOrigins.stream()
                .map(this::normalizeOrigin)
                .anyMatch(allowedOrigin -> allowedOrigin.equals(scheme + "://" + uri.getHost().toLowerCase() + ":" + effectivePort));
    }

    private String normalizeOrigin(String rawOrigin) {
        if (rawOrigin == null || rawOrigin.isBlank()) {
            return "";
        }
        String trimmed = rawOrigin.trim();
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme() == null ? "http" : uri.getScheme().toLowerCase();
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return "";
            }
            int effectivePort = uri.getPort() == -1 ? ("https".equals(scheme) ? 443 : 80) : uri.getPort();
            return scheme + "://" + host.toLowerCase() + ":" + effectivePort;
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private boolean isLocalhostHost(String host) {
        if (host == null) {
            return false;
        }
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
    }

}
