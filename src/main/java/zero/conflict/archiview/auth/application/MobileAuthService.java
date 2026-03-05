package zero.conflict.archiview.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .map(existing -> {
                    existing.updateName(info.name());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    if (info.email() == null || info.email().isBlank()) {
                        log.warn("소셜 로그인 이메일 누락 - provider={}, providerId={} ", provider, info.subject());
                        throw new DomainException(AuthErrorCode.PROVIDER_USERINFO_FAILED);
                    }

                    return userRepository.save(
                            User.builder()
                                    .email(info.email())
                                    .name(info.name())
                                    .provider(provider)
                                    .providerId(info.subject())
                                    .role(User.Role.GUEST)
                                    .build());
                });

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

    public record IdTokenInfo(String subject, String email, String name) {
    }
}
