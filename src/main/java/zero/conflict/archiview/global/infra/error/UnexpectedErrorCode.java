package zero.conflict.archiview.global.infra.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import zero.conflict.archiview.global.error.DomainErrorCode;

@Getter
@RequiredArgsConstructor
public enum UnexpectedErrorCode implements DomainErrorCode {

    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다."),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 API를 찾을 수 없습니다.")


    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
