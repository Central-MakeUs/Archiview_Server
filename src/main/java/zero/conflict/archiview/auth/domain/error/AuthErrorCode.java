package zero.conflict.archiview.auth.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import zero.conflict.archiview.global.error.DomainErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements DomainErrorCode {

    INVALID_PROVIDER_TOKEN("AUTH_001", "유효하지 않은 소셜 로그인 토큰입니다.", HttpStatus.UNAUTHORIZED),
    APPLE_CODE_EXCHANGE_FAILED("AUTH_002", "Apple 인증코드 교환에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    PROVIDER_USERINFO_FAILED("AUTH_003", "소셜 사용자 정보 조회에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    PROVIDER_ID_TOKEN_MISMATCH("AUTH_004", "소셜 토큰 검증 결과가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
