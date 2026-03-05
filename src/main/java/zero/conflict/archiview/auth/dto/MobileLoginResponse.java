package zero.conflict.archiview.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "MobileLoginResponse", description = "모바일 소셜 로그인 응답 데이터")
public record MobileLoginResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "사용자 ID", example = "00000000-0000-0000-0000-000000000001")
        UUID userId,
        @Schema(description = "이메일", example = "test@example.com")
        String email,
        @Schema(description = "이름", example = "testuser")
        String name
) {
}
