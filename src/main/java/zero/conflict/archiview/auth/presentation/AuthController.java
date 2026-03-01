package zero.conflict.archiview.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.dto.AppleMobileLoginRequest;
import zero.conflict.archiview.auth.dto.KakaoMobileLoginRequest;
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
@Tag(name = "Auth", description = "인증/로그인 API")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MobileAuthService mobileAuthService;
    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자 정보 조회
     */
    @Operation(summary = "현재 사용자 조회", description = "현재 인증된 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {

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
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "refreshToken을 포함한 요청 바디")
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
    @Operation(summary = "모바일 카카오 로그인", description = "카카오 Access Token으로 사용자 정보를 검증해 로그인합니다.")
    @PostMapping("/mobile/kakao")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mobileKakaoLogin(
            @Valid @RequestBody KakaoMobileLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mobileAuthService.loginWithKakao(request)));
    }

    /**
     * 모바일 애플 로그인 (ID Token 검증)
     */
    @Operation(summary = "모바일 애플 로그인", description = "애플 ID Token과 Authorization Code를 검증해 로그인합니다.")
    @PostMapping("/mobile/apple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mobileAppleLogin(
            @Valid @RequestBody AppleMobileLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mobileAuthService.loginWithApple(request)));
    }

    /**
     * 로그아웃
     */
    @Operation(summary = "로그아웃", description = "현재 사용자 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {

        log.info("사용자 로그아웃 - ID: {}", user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
    }
}
