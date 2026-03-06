package zero.conflict.archiview.auth.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import zero.conflict.archiview.auth.domain.error.AuthErrorCode;
import zero.conflict.archiview.auth.dto.KakaoMobileLoginRequest;
import zero.conflict.archiview.auth.dto.MobileLoginResponse;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.auth.infrastructure.apple.AppleAuthorizationCodeExchanger;
import zero.conflict.archiview.auth.infrastructure.apple.AppleIdTokenVerifier;
import zero.conflict.archiview.auth.infrastructure.kakao.KakaoAccessTokenVerifier;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MobileAuthServiceTest {

    @Mock
    private AppleAuthorizationCodeExchanger appleAuthorizationCodeExchanger;
    @Mock
    private AppleIdTokenVerifier appleIdTokenVerifier;
    @Mock
    private KakaoAccessTokenVerifier kakaoAccessTokenVerifier;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private MobileAuthService mobileAuthService;

    @Test
    @DisplayName("카카오 로그인 시 신규 사용자 이메일이 없으면 AUTH_003 예외를 반환한다")
    void loginWithKakao_withoutEmailForNewUser_throwsAuth003() throws Exception {
        KakaoMobileLoginRequest request = request("kakao-token");

        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new MobileAuthService.IdTokenInfo("kakao-subject", null, "kakao-user"));
        given(userRepository.findByProviderAndProviderId(User.OAuthProvider.KAKAO, "kakao-subject"))
                .willReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> mobileAuthService.loginWithKakao(request));

        assertThat(throwable).isInstanceOf(DomainException.class);
        assertThat(((DomainException) throwable).getErrorCode()).isEqualTo(AuthErrorCode.PROVIDER_USERINFO_FAILED);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("카카오 앱 변경으로 providerId가 달라도 이메일이 같으면 기존 카카오 계정으로 재연결한다")
    void loginWithKakao_relinkByEmail_whenProviderIdChanged() throws Exception {
        KakaoMobileLoginRequest request = request("kakao-token");
        UUID userId = UUID.randomUUID();
        User existing = User.builder()
                .id(userId)
                .email("kakao@example.com")
                .name("old-name")
                .provider(User.OAuthProvider.KAKAO)
                .providerId("old-provider-id")
                .role(User.Role.GUEST)
                .build();

        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new MobileAuthService.IdTokenInfo("new-provider-id", "kakao@example.com", "new-name"));
        given(userRepository.findByProviderAndProviderId(User.OAuthProvider.KAKAO, "new-provider-id"))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail("kakao@example.com"))
                .willReturn(Optional.of(existing));
        given(userRepository.save(existing)).willReturn(existing);
        given(jwtTokenProvider.createAccessToken(any())).willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken(userId)).willReturn("new-refresh-token");

        MobileLoginResponse response = mobileAuthService.loginWithKakao(request);

        assertThat(existing.getProviderId()).isEqualTo("new-provider-id");
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("이메일이 같아도 다른 소셜 계정이면 AUTH_006 예외를 반환한다")
    void loginWithKakao_emailAlreadyUsedByOtherProvider_throwsAuth006() throws Exception {
        KakaoMobileLoginRequest request = request("kakao-token");
        User existing = User.builder()
                .id(UUID.randomUUID())
                .email("same@example.com")
                .name("google-user")
                .provider(User.OAuthProvider.GOOGLE)
                .providerId("google-id")
                .role(User.Role.GUEST)
                .build();

        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new MobileAuthService.IdTokenInfo("new-kakao-id", "same@example.com", "kakao-user"));
        given(userRepository.findByProviderAndProviderId(User.OAuthProvider.KAKAO, "new-kakao-id"))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail("same@example.com"))
                .willReturn(Optional.of(existing));

        Throwable throwable = catchThrowable(() -> mobileAuthService.loginWithKakao(request));

        assertThat(throwable).isInstanceOf(DomainException.class);
        assertThat(((DomainException) throwable).getErrorCode())
                .isEqualTo(AuthErrorCode.ALREADY_REGISTERED_WITH_OTHER_PROVIDER);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("신규 가입 저장 중 unique 충돌이면 AUTH_005 예외를 반환한다")
    void loginWithKakao_duplicateDuringSave_throwsAuth005() throws Exception {
        KakaoMobileLoginRequest request = request("kakao-token");

        given(kakaoAccessTokenVerifier.verify("kakao-token"))
                .willReturn(new MobileAuthService.IdTokenInfo("kakao-subject", "dup@example.com", "kakao-user"));
        given(userRepository.findByProviderAndProviderId(User.OAuthProvider.KAKAO, "kakao-subject"))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail("dup@example.com")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("duplicate"));

        Throwable throwable = catchThrowable(() -> mobileAuthService.loginWithKakao(request));

        assertThat(throwable).isInstanceOf(DomainException.class);
        assertThat(((DomainException) throwable).getErrorCode()).isEqualTo(AuthErrorCode.ALREADY_REGISTERED_USER);
    }

    private KakaoMobileLoginRequest request(String accessToken) throws Exception {
        KakaoMobileLoginRequest request = new KakaoMobileLoginRequest();
        Field field = KakaoMobileLoginRequest.class.getDeclaredField("accessToken");
        field.setAccessible(true);
        field.set(request, accessToken);
        return request;
    }
}
