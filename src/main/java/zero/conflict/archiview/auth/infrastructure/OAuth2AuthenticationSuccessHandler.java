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
import java.util.Locale;
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
    @org.springframework.beans.factory.annotation.Value("#{'${auth.local-redirect-allowed-origins:http://localhost:3000,http://127.0.0.1:3000}'.split(',')}")
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
        if (!devRequested && authorizationRequest != null) {
            devRequested = Boolean.TRUE.equals(authorizationRequest.getAttributes().get("dev"));
        }
        boolean localhostSource = isLocalhostSourceByState(request);
        if (!localhostSource && authorizationRequest != null) {
            localhostSource = Boolean.TRUE.equals(authorizationRequest.getAttributes().get("localhostSource"));
        }
        boolean devAllowed = isDevAllowedEmail(oAuth2User.getUsername());
        String resolvedFrontendUrl = frontendUrl;
        if (devRequested && devAllowed && localhostSource) {
            Optional<String> localRedirectBaseUrl = resolveDevFrontendUrl(oAuth2User.getUsername());
            if (localRedirectBaseUrl.isPresent() && isAllowedLocalRedirectBaseUrl(localRedirectBaseUrl.get())) {
                resolvedFrontendUrl = localRedirectBaseUrl.get();
            }
        }

        log.info("OAuth2 로그인 리다이렉트 대상 결정 - devRequested: {}, localhostSource: {}, devAllowed: {}, frontendUrl: {}",
                devRequested, localhostSource, devAllowed, resolvedFrontendUrl);

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
            return true;
        } catch (URISyntaxException e) {
            log.warn("허용되지 않은 dev redirectUrl 형식 - value: {}", redirectUrl);
            return false;
        }
    }

    private boolean isAllowedLocalRedirectBaseUrl(String redirectUrl) {
        String normalizedRedirectOrigin = normalizeOrigin(redirectUrl);
        if (normalizedRedirectOrigin == null) {
            return false;
        }
        for (String allowedOriginRaw : localRedirectAllowedOrigins) {
            String normalizedAllowedOrigin = normalizeOrigin(allowedOriginRaw);
            if (normalizedRedirectOrigin.equals(normalizedAllowedOrigin)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeOrigin(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(rawUrl.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || host.isBlank()) {
                return null;
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
                return null;
            }
            if (uri.getUserInfo() != null || uri.getFragment() != null) {
                return null;
            }
            String path = uri.getPath();
            if (path != null && !path.isBlank() && !"/".equals(path)) {
                return null;
            }
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                return null;
            }
            int port = uri.getPort();
            if (port == -1) {
                port = "https".equals(normalizedScheme) ? 443 : 80;
            }
            return normalizedScheme + "://" + host.toLowerCase(Locale.ROOT) + ":" + port;
        } catch (URISyntaxException e) {
            return null;
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

        Object localhostSource = stateMap.remove(state);
        if (stateMap.isEmpty()) {
            session.removeAttribute(LOCALHOST_SOURCE_STATE_SESSION_KEY);
        }
        return Boolean.TRUE.equals(localhostSource);
    }
}
