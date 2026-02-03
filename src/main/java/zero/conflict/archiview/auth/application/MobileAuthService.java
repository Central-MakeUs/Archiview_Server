package zero.conflict.archiview.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.auth.dto.MobileLoginRequest;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.auth.infrastructure.apple.AppleIdTokenVerifier;
import zero.conflict.archiview.auth.infrastructure.kakao.KakaoIdTokenVerifier;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MobileAuthService {

    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final KakaoIdTokenVerifier kakaoIdTokenVerifier;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Map<String, Object> loginWithApple(MobileLoginRequest request) {
        var info = appleIdTokenVerifier.verify(request.getIdToken());
        return loginWithProvider(info, User.OAuthProvider.APPLE, request.getRole());
    }

    @Transactional
    public Map<String, Object> loginWithKakao(MobileLoginRequest request) {
        var info = kakaoIdTokenVerifier.verify(request.getIdToken());
        return loginWithProvider(info, User.OAuthProvider.KAKAO, request.getRole());
    }

    private Map<String, Object> loginWithProvider(
            IdTokenInfo info,
            User.OAuthProvider provider,
            String roleParam) {
        User.Role role = resolveRole(roleParam);

        User user = userRepository.findByProviderAndProviderId(provider, info.subject())
                .map(existing -> {
                    existing.updateName(info.name());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(info.email())
                                .name(info.name())
                                .provider(provider)
                                .providerId(info.subject())
                                .role(role)
                                .build()));

        String accessToken = jwtTokenProvider.createAccessToken(
                new zero.conflict.archiview.auth.domain.CustomOAuth2User(user, new HashMap<>()));
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", accessToken);
        tokenResponse.put("refreshToken", refreshToken);
        tokenResponse.put("userId", user.getId());
        tokenResponse.put("email", user.getEmail());
        tokenResponse.put("name", user.getName());

        return tokenResponse;
    }

    private User.Role resolveRole(String roleParam) {
        if (roleParam == null || roleParam.isBlank()) {
            return User.Role.GUEST;
        }
        try {
            return User.Role.valueOf(roleParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.Role.GUEST;
        }
    }

    public record IdTokenInfo(String subject, String email, String name) {
    }
}
