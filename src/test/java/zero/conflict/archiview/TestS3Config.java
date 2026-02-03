package zero.conflict.archiview;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import zero.conflict.archiview.global.infra.s3.PresignedUrlInfo;
import zero.conflict.archiview.global.infra.s3.S3Service;

@Configuration
@Profile("test")
public class TestS3Config {

    @Bean(name = "s3Service")
    public S3Service s3Service() {
        S3Service s3Service = Mockito.mock(S3Service.class);
        Mockito.when(s3Service.generatePresignedUploadUrl(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(new PresignedUrlInfo("http://localhost/test-upload", "test-key", 300));
        return s3Service;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        org.springframework.security.oauth2.client.registration.ClientRegistration registration =
                org.springframework.security.oauth2.client.registration.ClientRegistration.withRegistrationId("test")
                        .clientId("test-client-id")
                        .clientSecret("test-client-secret")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("http://localhost/login/oauth2/code/test")
                        .authorizationUri("http://localhost/oauth/authorize")
                        .tokenUri("http://localhost/oauth/token")
                        .userInfoUri("http://localhost/userinfo")
                        .userNameAttributeName("id")
                        .clientName("test")
                        .scope("profile")
                        .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
