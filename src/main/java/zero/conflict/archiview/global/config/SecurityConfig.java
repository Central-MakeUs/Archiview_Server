package zero.conflict.archiview.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import zero.conflict.archiview.auth.application.CustomOAuth2UserService;
import zero.conflict.archiview.auth.infrastructure.JwtAuthenticationFilter;
import zero.conflict.archiview.auth.infrastructure.OAuth2AuthenticationFailureHandler;
import zero.conflict.archiview.auth.infrastructure.OAuth2AuthenticationSuccessHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
        private static final String DEV_LOGIN_STATE_SESSION_KEY = "OAUTH2_DEV_LOGIN_STATE_MAP";
        private static final String LOCALHOST_SOURCE_STATE_SESSION_KEY = "OAUTH2_LOCALHOST_SOURCE_STATE_MAP";

        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
        private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Value("#{'${auth.local-redirect-allowed-origins:http://localhost:3000,http://127.0.0.1:3000}'.split(',')}")
        private List<String> localRedirectAllowedOrigins;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/error", "/favicon.ico", "/health").permitAll()
                                                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/auth/**",
                                                                "/api/v1/auth/mobile/**",
                                                                "/api/v1/auth/refresh",
                                                                "/api/v1/auth/test/**",
                                                                "/oauth2/**",
                                                                "/login/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .authorizationRequestRepository(
                                                                                authorizationRequestRepository())
                                                                .authorizationRequestResolver(
                                                                                customAuthorizationRequestResolver(
                                                                                                http.getSharedObject(
                                                                                                                ClientRegistrationRepository.class))))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler)
                                                .failureHandler(oAuth2FailureHandler))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
                return new HttpSessionOAuth2AuthorizationRequestRepository();
        }

        @Bean
        public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
                        ClientRegistrationRepository clientRegistrationRepository) {

                DefaultOAuth2AuthorizationRequestResolver defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                                clientRegistrationRepository, "/oauth2/authorization");

                return new OAuth2AuthorizationRequestResolver() {
                        @Override
                        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                                return customizeAuthorizationRequest(authorizationRequest, request);
                        }

                        @Override
                        public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
                                        String clientRegistrationId) {
                                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request,
                                                clientRegistrationId);
                                return customizeAuthorizationRequest(authorizationRequest, request);
                        }

                        private OAuth2AuthorizationRequest customizeAuthorizationRequest(
                                        OAuth2AuthorizationRequest authorizationRequest,
                                        HttpServletRequest request) {
                                if (authorizationRequest == null) {
                                        return null;
                                }

                                Map<String, Object> additionalParameters = new HashMap<>(
                                                authorizationRequest.getAdditionalParameters());
                                Map<String, Object> attributes = new HashMap<>(
                                                authorizationRequest.getAttributes());
                                String role = request.getParameter("role");
                                if (role != null) {
                                        additionalParameters.put("role", role);
                                }
                                String state = authorizationRequest.getState();
                                if ("true".equalsIgnoreCase(request.getParameter("dev"))) {
                                        attributes.put("dev", true);
                                        if (state != null && !state.isBlank()) {
                                                @SuppressWarnings("unchecked")
                                                Map<String, Boolean> devStateMap = (Map<String, Boolean>) request
                                                                .getSession(true)
                                                                .getAttribute(DEV_LOGIN_STATE_SESSION_KEY);
                                                if (devStateMap == null) {
                                                        devStateMap = new HashMap<>();
                                                }
                                                devStateMap.put(state, true);
                                                request.getSession(true).setAttribute(DEV_LOGIN_STATE_SESSION_KEY,
                                                                devStateMap);
                                        }
                                }
                                boolean localhostSource = isLocalhostSourceRequest(request);
                                attributes.put("localhostSource", localhostSource);
                                if (state != null && !state.isBlank()) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Boolean> localhostStateMap = (Map<String, Boolean>) request
                                                        .getSession(true)
                                                        .getAttribute(LOCALHOST_SOURCE_STATE_SESSION_KEY);
                                        if (localhostStateMap == null) {
                                                localhostStateMap = new HashMap<>();
                                        }
                                        localhostStateMap.put(state, localhostSource);
                                        request.getSession(true).setAttribute(LOCALHOST_SOURCE_STATE_SESSION_KEY,
                                                        localhostStateMap);
                                }
                                warnIfMobileAppUsesWebOAuth(request);

                                return OAuth2AuthorizationRequest.from(authorizationRequest)
                                                .additionalParameters(additionalParameters)
                                                .attributes(attributes)
                                                .build();
                        }

                        private void warnIfMobileAppUsesWebOAuth(HttpServletRequest request) {
                                String requestUri = request.getRequestURI();
                                boolean kakaoWebOAuthEndpoint = requestUri != null
                                                && requestUri.startsWith("/oauth2/authorization/kakao");
                                if (!kakaoWebOAuthEndpoint) {
                                        return;
                                }
                                if (!isLikelyMobileAppRequest(request)) {
                                        return;
                                }
                                log.warn(
                                                "모바일 앱으로 추정되는 요청이 웹 OAuth 엔드포인트로 진입했습니다. uri={}, userAgent={}, xClientPlatform={}, xAppClient={}. 모바일 앱은 /api/v1/auth/mobile/kakao 사용이 권장됩니다.",
                                                requestUri,
                                                request.getHeader("User-Agent"),
                                                request.getHeader("X-Client-Platform"),
                                                request.getHeader("X-App-Client"));
                        }

                        private boolean isLikelyMobileAppRequest(HttpServletRequest request) {
                                String clientPlatform = headerValue(request, "X-Client-Platform");
                                if ("android".equals(clientPlatform) || "ios".equals(clientPlatform)
                                                || "mobile-app".equals(clientPlatform) || "app".equals(clientPlatform)) {
                                        return true;
                                }
                                String appClient = headerValue(request, "X-App-Client");
                                if ("true".equals(appClient) || "1".equals(appClient)) {
                                        return true;
                                }
                                String requestedFromApp = headerValue(request, "X-Requested-From-App");
                                if ("true".equals(requestedFromApp) || "1".equals(requestedFromApp)) {
                                        return true;
                                }
                                String userAgent = headerValue(request, "User-Agent");
                                return userAgent.contains("okhttp")
                                                || userAgent.contains("dalvik")
                                                || userAgent.contains("cfnetwork")
                                                || userAgent.contains("kakaotalk")
                                                || userAgent.contains("androidapp")
                                                || userAgent.contains("iosapp");
                        }

                        private String headerValue(HttpServletRequest request, String name) {
                                String value = request.getHeader(name);
                                if (value == null || value.isBlank()) {
                                        return "";
                                }
                                return value.trim().toLowerCase();
                        }

                        private boolean isLocalhostSourceRequest(HttpServletRequest request) {
                                String origin = extractOrigin(request.getHeader("Origin"));
                                if (isAllowedLocalOrigin(origin)) {
                                        return true;
                                }

                                String referer = request.getHeader("Referer");
                                if (referer != null && !referer.isBlank()) {
                                        try {
                                                java.net.URI refererUri = java.net.URI.create(referer);
                                                String refererOrigin = refererUri.getScheme() + "://"
                                                                + refererUri.getHost()
                                                                + (refererUri.getPort() == -1 ? "" : ":" + refererUri.getPort());
                                                return isAllowedLocalOrigin(refererOrigin);
                                        } catch (IllegalArgumentException ignored) {
                                                return false;
                                        }
                                }
                                return false;
                        }

                        private boolean isAllowedLocalOrigin(String origin) {
                                if (origin == null || origin.isBlank()) {
                                        return false;
                                }
                                String normalizedOrigin = normalizeOrigin(origin);
                                return localRedirectAllowedOrigins.stream()
                                                .map(this::normalizeOrigin)
                                                .anyMatch(normalizedOrigin::equals);
                        }

                        private String extractOrigin(String originHeader) {
                                if (originHeader == null || originHeader.isBlank()) {
                                        return null;
                                }
                                return originHeader.trim();
                        }

                        private String normalizeOrigin(String rawOrigin) {
                                if (rawOrigin == null || rawOrigin.isBlank()) {
                                        return "";
                                }
                                try {
                                        java.net.URI uri = java.net.URI.create(rawOrigin.trim());
                                        if (uri.getScheme() == null || uri.getHost() == null) {
                                                return "";
                                        }
                                        String scheme = uri.getScheme().toLowerCase();
                                        String host = uri.getHost().toLowerCase();
                                        int effectivePort = uri.getPort() == -1
                                                        ? ("https".equals(scheme) ? 443 : 80)
                                                        : uri.getPort();
                                        return scheme + "://" + host + ":" + effectivePort;
                                } catch (IllegalArgumentException e) {
                                        return "";
                                }
                        }
                };
        }
}
