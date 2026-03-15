package zero.conflict.archiview.auth.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.dto.AppleMobileLoginRequest;
import zero.conflict.archiview.auth.dto.KakaoMobileLoginRequest;
import zero.conflict.archiview.auth.dto.MobileLoginResponse;
import zero.conflict.archiview.auth.dto.RefreshTokenRequest;
import zero.conflict.archiview.auth.application.MobileAuthService;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final JwtTokenProvider jwtTokenProvider;
    private final MobileAuthService mobileAuthService;
    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자 정보 조회
     */
    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User user) {

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("email", user.getUsername());
        response.put("name", user.getName());
        response.put("provider", user.getUser().getProvider());
        response.put("role", user.getUser().getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.fail("INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh Token입니다.")
            );
        }

        java.util.UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.fail("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
            );
        }
        String newAccessToken = jwtTokenProvider.createAccessToken(
                new CustomOAuth2User(user, new HashMap<>()));

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", refreshToken
        )));
    }

    /**
     * 모바일 카카오 로그인 (ID Token 검증)
     */
    @Override
    @PostMapping("/mobile/kakao")
    public ResponseEntity<ApiResponse<MobileLoginResponse>> mobileKakaoLogin(
            @Valid @RequestBody KakaoMobileLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mobileAuthService.loginWithKakao(request)));
    }

    /**
     * 모바일 애플 로그인 (ID Token 검증)
     */
    @Override
    @PostMapping("/mobile/apple")
    public ResponseEntity<ApiResponse<MobileLoginResponse>> mobileAppleLogin(
            @Valid @RequestBody AppleMobileLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mobileAuthService.loginWithApple(request)));
    }

    /**
     * 로그아웃
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomOAuth2User user) {

        log.info("사용자 로그아웃 - ID: {}", user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
    }
}
