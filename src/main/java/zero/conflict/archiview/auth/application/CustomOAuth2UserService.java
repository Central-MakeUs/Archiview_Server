package zero.conflict.archiview.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.domain.OAuth2UserInfo;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2User.getAttributes());

        // ✅ role 파라미터 추출
        String roleParam = userRequest.getAdditionalParameters().getOrDefault("role", "archivier").toString();
        User.Role role = User.Role.valueOf(roleParam.toUpperCase());

        User user = getOrCreateUser(oAuth2UserInfo, role);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User getOrCreateUser(OAuth2UserInfo oAuth2UserInfo, User.Role role) {
        User.OAuthProvider provider = User.OAuthProvider.valueOf(oAuth2UserInfo.getProvider().toUpperCase());

        return userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getProviderId())
                .map(user -> {
                    user.updateProfile(oAuth2UserInfo.getName(), oAuth2UserInfo.getProfileImageUrl());
                    return userRepository.save(user);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(oAuth2UserInfo.getEmail())
                                .name(oAuth2UserInfo.getName())
                                .profileImageUrl(oAuth2UserInfo.getProfileImageUrl())
                                .provider(provider)
                                .providerId(oAuth2UserInfo.getProviderId())
                                .role(role) // ✅ 역할 적용
                                .build()
                ));
    }
}
