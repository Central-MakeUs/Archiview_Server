package zero.conflict.archiview.global.infra.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zero.conflict.archiview.global.error.DomainErrorCode;
import zero.conflict.archiview.global.error.DomainException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
        log.error("DomainException: ", e);

        DomainErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(
                errorCode.getCode(),
                e.getMessage(),
                errorCode.getStatus()
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        DomainErrorCode errorCode = UnexpectedErrorCode.UNEXPECTED_ERROR;
        ErrorResponse response = ErrorResponse.of(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus()
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}