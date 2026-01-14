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
}

