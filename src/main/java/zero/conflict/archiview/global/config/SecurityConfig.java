package zero.conflict.archiview.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private static final String DEV_LOGIN_STATE_SESSION_KEY = "OAUTH2_DEV_LOGIN_STATE_MAP";

        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
        private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
                                if ("true".equalsIgnoreCase(request.getParameter("dev"))) {
                                        attributes.put("dev", true);
                                        String state = authorizationRequest.getState();
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

                                return OAuth2AuthorizationRequest.from(authorizationRequest)
                                                .additionalParameters(additionalParameters)
                                                .attributes(attributes)
                                                .build();
                        }
                };
        }
}
