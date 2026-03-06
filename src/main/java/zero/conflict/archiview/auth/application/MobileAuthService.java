package zero.conflict.archiview.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.auth.domain.error.AuthErrorCode;
import zero.conflict.archiview.auth.dto.AppleMobileLoginRequest;
import zero.conflict.archiview.auth.dto.KakaoMobileLoginRequest;
import zero.conflict.archiview.auth.dto.MobileLoginResponse;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.auth.infrastructure.apple.AppleAuthorizationCodeExchanger;
import zero.conflict.archiview.auth.infrastructure.apple.AppleIdTokenVerifier;
import zero.conflict.archiview.auth.infrastructure.kakao.KakaoAccessTokenVerifier;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MobileAuthService {

    private final AppleAuthorizationCodeExchanger appleAuthorizationCodeExchanger;
    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final KakaoAccessTokenVerifier kakaoAccessTokenVerifier;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MobileLoginResponse loginWithApple(AppleMobileLoginRequest request) {
        IdTokenInfo appProvidedInfo = appleIdTokenVerifier.verify(request.getIdToken());
        String exchangedIdToken = appleAuthorizationCodeExchanger.exchangeForIdToken(request.getAuthorizationCode());
        IdTokenInfo exchangedInfo = appleIdTokenVerifier.verify(exchangedIdToken);

        if (!Objects.equals(appProvidedInfo.subject(), exchangedInfo.subject())) {
            throw new DomainException(AuthErrorCode.PROVIDER_ID_TOKEN_MISMATCH);
        }
        return loginWithProvider(exchangedInfo, User.OAuthProvider.APPLE);
    }

    @Transactional
    public MobileLoginResponse loginWithKakao(KakaoMobileLoginRequest request) {
        var info = kakaoAccessTokenVerifier.verify(request.getAccessToken());
        return loginWithProvider(info, User.OAuthProvider.KAKAO);
    }

    private MobileLoginResponse loginWithProvider(
            IdTokenInfo info,
            User.OAuthProvider provider) {
        User user = userRepository.findByProviderAndProviderId(provider, info.subject())
                .map(existing -> updateUserName(existing, info.name()))
                .orElseGet(() -> findOrCreateUserByEmail(info, provider));

        String accessToken = jwtTokenProvider.createAccessToken(
                new zero.conflict.archiview.auth.domain.CustomOAuth2User(user, new HashMap<>()));
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new MobileLoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getName());
    }

    private User findOrCreateUserByEmail(IdTokenInfo info, User.OAuthProvider provider) {
        String email = requireEmail(info, provider);
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User existing = userByEmail.get();
            if (existing.getProvider() != provider) {
                log.warn("소셜 로그인 provider 충돌 - email={}, existingProvider={}, requestProvider={}",
                        maskEmail(email),
                        existing.getProvider(),
                        provider);
                throw new DomainException(AuthErrorCode.ALREADY_REGISTERED_WITH_OTHER_PROVIDER);
            }

            String previousProviderId = existing.getProviderId();
            if (!Objects.equals(previousProviderId, info.subject())) {
                existing.relinkSocialAccount(provider, info.subject());
                log.info("소셜 계정 재연결 완료 - provider={}, email={}, oldProviderId={}, newProviderId={}",
                        provider,
                        maskEmail(email),
                        previousProviderId,
                        info.subject());
            }
            existing.updateName(info.name());
            return saveWithDuplicateHandling(existing);
        }

        return saveWithDuplicateHandling(User.builder()
                .email(email)
                .name(info.name())
                .provider(provider)
                .providerId(info.subject())
                .role(User.Role.GUEST)
                .build());
    }

    private String requireEmail(IdTokenInfo info, User.OAuthProvider provider) {
        if (info.email() == null || info.email().isBlank()) {
            log.warn("소셜 로그인 이메일 누락 - provider={}, providerId={}", provider, info.subject());
            throw new DomainException(AuthErrorCode.PROVIDER_USERINFO_FAILED);
        }
        return info.email();
    }

    private User updateUserName(User user, String name) {
        user.updateName(name);
        return saveWithDuplicateHandling(user);
    }

    private User saveWithDuplicateHandling(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("소셜 로그인 저장 중 unique 충돌 - provider={}, providerId={}, email={}",
                    user.getProvider(),
                    user.getProviderId(),
                    maskEmail(user.getEmail()));
            throw new DomainException(AuthErrorCode.ALREADY_REGISTERED_USER);
        }
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

    public record IdTokenInfo(String subject, String email, String name) {
    }
}
