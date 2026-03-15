package zero.conflict.archiview.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.dto.AppleMobileLoginRequest;
import zero.conflict.archiview.auth.dto.KakaoMobileLoginRequest;
import zero.conflict.archiview.auth.dto.MobileLoginResponse;
import zero.conflict.archiview.auth.dto.RefreshTokenRequest;
import zero.conflict.archiview.global.infra.response.ApiResponse;

import java.util.Map;

@Tag(name = "Auth", description = "인증/로그인 API")
public interface AuthApi {

    @Operation(summary = "현재 사용자 조회",
            description = "현재 인증된 사용자의 userId, email, name, provider, role 정보를 조회합니다.")
    ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "토큰 재발급",
            description = "Refresh Token으로 새로운 Access Token을 발급합니다. 응답은 accessToken과 refreshToken을 함께 반환합니다.")
    ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestBody(description = "refreshToken 문자열을 포함한 요청 바디")
            @Valid RefreshTokenRequest request);

    @Operation(summary = "모바일 카카오 로그인",
            description = "카카오 Access Token으로 사용자 정보를 검증하고 로그인/회원가입 후 앱용 토큰을 발급합니다.")
    ResponseEntity<ApiResponse<MobileLoginResponse>> mobileKakaoLogin(
            @RequestBody(description = "카카오 Access Token 요청")
            @Valid KakaoMobileLoginRequest request);

    @Operation(summary = "모바일 애플 로그인",
            description = "애플 ID Token과 Authorization Code를 검증하고 로그인/회원가입 후 앱용 토큰을 발급합니다.")
    ResponseEntity<ApiResponse<MobileLoginResponse>> mobileAppleLogin(
            @RequestBody(description = "애플 로그인 요청")
            @Valid AppleMobileLoginRequest request);

    @Operation(summary = "로그아웃",
            description = "현재 사용자 로그아웃 처리용 API입니다. 서버 세션 무효화 대신 클라이언트 토큰 폐기 흐름에서 사용합니다.")
    ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);
}
