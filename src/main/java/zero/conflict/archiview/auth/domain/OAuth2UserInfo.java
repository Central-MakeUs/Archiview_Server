package zero.conflict.archiview.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class OAuth2UserInfo {
    private final String providerId;
    private final String provider;
    private final String email;
    private final String name;
    private final String profileImageUrl;

    public static OAuth2UserInfo of(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "apple" -> ofApple(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + provider);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                (String) attributes.get("sub"),
                "google",
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }

    private static OAuth2UserInfo ofApple(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                (String) attributes.get("sub"),
                "apple",
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                null // Apple은 프로필 이미지를 제공하지 않음
        );
    }

    @SuppressWarnings("unchecked")
    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount == null
                ? null
                : (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = profile == null ? null : (String) profile.get("nickname");
        String profileImageUrl = profile == null ? null : (String) profile.get("profile_image_url");
        String email = kakaoAccount == null ? null : (String) kakaoAccount.get("email");

        return new OAuth2UserInfo(
                String.valueOf(attributes.get("id")),
                "kakao",
                email,
                nickname,
                profileImageUrl
        );
    }
}
