package zero.conflict.archiview.auth.infrastructure.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.auth.application.MobileAuthService.IdTokenInfo;
import zero.conflict.archiview.auth.infrastructure.jwt.AudienceValidator;

@Component
public class KakaoIdTokenVerifier {

    private final JwtDecoder jwtDecoder;

    public KakaoIdTokenVerifier(
            @Value("${auth.mobile.kakao.issuer}") String issuer,
            @Value("${auth.mobile.kakao.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://kauth.kakao.com/.well-known/jwks.json")
                .build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new AudienceValidator(audience)
        );
        decoder.setJwtValidator(validator);
        this.jwtDecoder = decoder;
    }

    public IdTokenInfo verify(String idToken) {
        Jwt jwt = jwtDecoder.decode(idToken);
        String name = jwt.getClaimAsString("nickname");
        return new IdTokenInfo(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                name == null ? "kakao-user" : name
        );
    }
}
