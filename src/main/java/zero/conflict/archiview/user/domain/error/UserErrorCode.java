package zero.conflict.archiview.user.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import zero.conflict.archiview.global.error.DomainErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements DomainErrorCode {

    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ALREADY_ONBOARDED("USER_002", "이미 온보딩을 완료한 사용자입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
