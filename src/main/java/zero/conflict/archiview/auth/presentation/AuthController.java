package zero.conflict.archiview.auth.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.presentation.dto.MobileLoginRequest;
import zero.conflict.archiview.auth.application.MobileAuthService;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MobileAuthService mobileAuthService;

    /**
     * 현재 인증된 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal CustomOAuth2User user) {

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("email", user.getUsername());
        response.put("name", user.getName());
        response.put("provider", user.getUser().getProvider());
        response.put("role", user.getUser().getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "유효하지 않은 Refresh Token입니다.")
            );
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(
                new CustomOAuth2User(null, new HashMap<>()) {
                    @Override
                    public Long getUserId() {
                        return userId;
                    }
                }
        );

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", refreshToken
        ));
    }

    /**
     * 모바일 카카오 로그인 (ID Token 검증)
     */
    @PostMapping("/mobile/kakao")
    public ResponseEntity<Map<String, Object>> mobileKakaoLogin(
            @Valid @RequestBody MobileLoginRequest request) {
        return ResponseEntity.ok(mobileAuthService.loginWithKakao(request));
    }

    /**
     * 모바일 애플 로그인 (ID Token 검증)
     */
    @PostMapping("/mobile/apple")
    public ResponseEntity<Map<String, Object>> mobileAppleLogin(
            @Valid @RequestBody MobileLoginRequest request) {
        return ResponseEntity.ok(mobileAuthService.loginWithApple(request));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal CustomOAuth2User user) {

        log.info("사용자 로그아웃 - ID: {}", user.getUserId());

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }
}
