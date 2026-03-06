package zero.conflict.archiview.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.auth.domain.error.AuthErrorCode;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.domain.OAuth2UserInfo;
import zero.conflict.archiview.auth.infrastructure.jwt.AudienceValidator;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = "apple".equalsIgnoreCase(registrationId)
                ? extractAppleAttributes(userRequest)
                : super.loadUser(userRequest).getAttributes();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);

        // ✅ 최초 로그인 시 기본 역할은 GUEST로 설정
        User user = getOrCreateUser(oAuth2UserInfo, User.Role.GUEST);

        return new CustomOAuth2User(user, attributes);
    }

    private Map<String, Object> extractAppleAttributes(OAuth2UserRequest userRequest) {
        Object idTokenValue = userRequest.getAdditionalParameters().get("id_token");
        if (!(idTokenValue instanceof String idToken)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Missing id_token", null));
        }

        JwtDecoder jwtDecoder = buildAppleJwtDecoder(userRequest.getClientRegistration().getClientId());
        Jwt jwt = jwtDecoder.decode(idToken);

        Map<String, Object> attributes = new HashMap<>(jwt.getClaims());
        attributes.putIfAbsent("name", "apple-user");
        return attributes;
    }

    private JwtDecoder buildAppleJwtDecoder(String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://appleid.apple.com/auth/keys")
                .build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer("https://appleid.apple.com"),
                new AudienceValidator(clientId)
        );
        decoder.setJwtValidator(validator);
        return decoder;
    }

    private User getOrCreateUser(OAuth2UserInfo oAuth2UserInfo, User.Role role) {
        User.OAuthProvider provider = User.OAuthProvider.valueOf(oAuth2UserInfo.getProvider().toUpperCase());

        return userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getProviderId())
                .map(user -> {
                    user.updateName(oAuth2UserInfo.getName());
                    return saveWithDuplicateHandling(user);
                })
                .orElseGet(() -> getOrRelinkByEmail(oAuth2UserInfo, provider, role));
    }

    private User getOrRelinkByEmail(OAuth2UserInfo oAuth2UserInfo, User.OAuthProvider provider, User.Role role) {
        String email = oAuth2UserInfo.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("웹 소셜 로그인 이메일 누락 - provider={}, providerId={}", provider, oAuth2UserInfo.getProviderId());
            throw oauth2Exception(AuthErrorCode.PROVIDER_USERINFO_FAILED);
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User existing = userByEmail.get();
            if (existing.getProvider() != provider) {
                log.warn("웹 소셜 로그인 provider 충돌 - email={}, existingProvider={}, requestProvider={}",
                        maskEmail(email),
                        existing.getProvider(),
                        provider);
                throw oauth2Exception(AuthErrorCode.ALREADY_REGISTERED_WITH_OTHER_PROVIDER);
            }

            String previousProviderId = existing.getProviderId();
            if (!Objects.equals(previousProviderId, oAuth2UserInfo.getProviderId())) {
                existing.relinkSocialAccount(provider, oAuth2UserInfo.getProviderId());
                log.info("웹 소셜 계정 재연결 완료 - provider={}, email={}, oldProviderId={}, newProviderId={}",
                        provider,
                        maskEmail(email),
                        previousProviderId,
                        oAuth2UserInfo.getProviderId());
            }
            existing.updateName(oAuth2UserInfo.getName());
            return saveWithDuplicateHandling(existing);
        }

        return saveWithDuplicateHandling(User.builder()
                .email(email)
                .name(oAuth2UserInfo.getName())
                .provider(provider)
                .providerId(oAuth2UserInfo.getProviderId())
                .role(role)
                .build());
    }

    private User saveWithDuplicateHandling(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("웹 소셜 로그인 저장 중 unique 충돌 - provider={}, providerId={}, email={}",
                    user.getProvider(),
                    user.getProviderId(),
                    maskEmail(user.getEmail()));
            throw oauth2Exception(AuthErrorCode.ALREADY_REGISTERED_USER);
        }
    }

    private OAuth2AuthenticationException oauth2Exception(AuthErrorCode errorCode) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode.getCode(), errorCode.getMessage(), null));
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "[empty]";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }
}
