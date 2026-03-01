package zero.conflict.archiview.auth.infrastructure.apple;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import zero.conflict.archiview.auth.domain.error.AuthErrorCode;
import zero.conflict.archiview.global.error.DomainException;

import java.util.Map;

@Slf4j
@Component
public class AppleAuthorizationCodeExchanger {

    private final RestClient restClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;

    public AppleAuthorizationCodeExchanger(
            @Value("${auth.mobile.apple.token-uri:https://appleid.apple.com/auth/token}") String tokenUri,
            @Value("${auth.mobile.apple.client-id:}") String clientId,
            @Value("${auth.mobile.apple.client-secret:}") String clientSecret) {
        this.restClient = RestClient.create();
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String exchangeForIdToken(String authorizationCode) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            log.error("Apple 모바일 인증 설정 누락 - clientId/clientSecret");
            throw new DomainException(AuthErrorCode.APPLE_CODE_EXCHANGE_FAILED);
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", authorizationCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            String idToken = readString(response, "id_token");
            if (idToken == null || idToken.isBlank()) {
                throw new DomainException(AuthErrorCode.APPLE_CODE_EXCHANGE_FAILED);
            }
            return idToken;
        } catch (RestClientException e) {
            log.warn("Apple 인증코드 교환 실패", e);
            throw new DomainException(AuthErrorCode.APPLE_CODE_EXCHANGE_FAILED);
        }
    }

    private String readString(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }
}
