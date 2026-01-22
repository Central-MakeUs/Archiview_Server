package zero.conflict.archiview.global.infra.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zero.conflict.archiview.global.error.DomainErrorCode;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.global.infra.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        log.error("DomainException: ", e);

        DomainErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), e.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: ", e);

        String errorMessage = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        ApiResponse<Void> response = ApiResponse.fail("VALIDATION_ERROR", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        DomainErrorCode errorCode = UnexpectedErrorCode.UNEXPECTED_ERROR;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
