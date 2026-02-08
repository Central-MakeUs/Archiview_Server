package zero.conflict.archiview.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/test")
@RequiredArgsConstructor
@Tag(name = "Auth Test", description = "테스트용 인증 API")
public class TestAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private static final long ONE_MONTH = 1000L * 60 * 60 * 24 * 30;

    @Operation(summary = "테스트용 에디터 로그인", description = "EDITOR 권한의 테스트 계정 토큰을 1개월 만료 기한으로 발급합니다.")
    @GetMapping("/editor")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEditorTestToken() {
        return createTestToken("test-editor@archiview.com", "테스트 에디터", User.Role.EDITOR);
    }

    @Operation(summary = "테스트용 아카이버 로그인", description = "ARCHIVER 권한의 테스트 계정 토큰을 1개월 만료 기한으로 발급합니다.")
    @GetMapping("/archiver")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getArchiverTestToken() {
        return createTestToken("test-archiver@archiview.com", "테스트 아카이버", User.Role.ARCHIVER);
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> createTestToken(String email, String name,
            User.Role role) {
        User user = userRepository.findByProviderAndProviderId(User.OAuthProvider.GOOGLE, email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .provider(User.OAuthProvider.GOOGLE)
                                .providerId(email)
                                .role(role)
                                .build()));

        // 권한이 바뀌었을 수도 있으니 강제 업데이트
        if (user.getRole() != role) {
            user.assignRole(role);
            userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                new CustomOAuth2User(user, new HashMap<>()), ONE_MONTH);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        log.info("테스트용 토큰 발급 - 사용자: {}, 역할: {}, 만료기한: 1개월", email, role);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
