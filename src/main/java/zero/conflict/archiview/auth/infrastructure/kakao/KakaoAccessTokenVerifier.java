package zero.conflict.archiview.auth.infrastructure.kakao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import zero.conflict.archiview.auth.application.MobileAuthService.IdTokenInfo;
import zero.conflict.archiview.auth.domain.error.AuthErrorCode;
import zero.conflict.archiview.global.error.DomainException;

import java.util.Map;

@Slf4j
@Component
public class KakaoAccessTokenVerifier {

    private final RestClient restClient;
    private final String userInfoUri;

    public KakaoAccessTokenVerifier(
            @Value("${auth.mobile.kakao.user-info-uri:https://kapi.kakao.com/v2/user/me}") String userInfoUri) {
        this.restClient = RestClient.create();
        this.userInfoUri = userInfoUri;
    }

    public IdTokenInfo verify(String accessToken) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            String subject = extractSubject(response);
            if (subject == null || subject.isBlank()) {
                throw new DomainException(AuthErrorCode.PROVIDER_USERINFO_FAILED);
            }
            String email = extractEmail(response);
            String name = extractName(response);
            return new IdTokenInfo(subject, email, name == null ? "kakao-user" : name);
        } catch (RestClientException e) {
            log.warn("Kakao 사용자 정보 조회 실패", e);
            throw new DomainException(AuthErrorCode.INVALID_PROVIDER_TOKEN);
        }
    }

    private String extractSubject(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        Object id = payload.get("id");
        return id == null ? null : String.valueOf(id);
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        Object kakaoAccount = payload.get("kakao_account");
        if (!(kakaoAccount instanceof Map<?, ?> accountMap)) {
            return null;
        }
        Object email = ((Map<String, Object>) accountMap).get("email");
        return email instanceof String ? (String) email : null;
    }

    @SuppressWarnings("unchecked")
    private String extractName(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        Object kakaoAccount = payload.get("kakao_account");
        if (!(kakaoAccount instanceof Map<?, ?> accountMap)) {
            return null;
        }
        Object profile = ((Map<String, Object>) accountMap).get("profile");
        if (!(profile instanceof Map<?, ?> profileMap)) {
            return null;
        }
        Object nickname = ((Map<String, Object>) profileMap).get("nickname");
        return nickname instanceof String ? (String) nickname : null;
    }
}
