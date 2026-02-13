package zero.conflict.archiview.global.infra.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zero.conflict.archiview.global.error.DomainErrorCode;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: ", e);

        if (isInvalidUserRoleValue(e)) {
            ApiResponse<Void> response = ApiResponse.fail(
                    UserErrorCode.INVALID_ROLE_SWITCH_TARGET.getCode(),
                    UserErrorCode.INVALID_ROLE_SWITCH_TARGET.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        ApiResponse<Void> response = ApiResponse.fail("VALIDATION_ERROR", "입력값이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);

        DomainErrorCode errorCode = UnexpectedErrorCode.UNEXPECTED_ERROR;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    private boolean isInvalidUserRoleValue(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof InvalidFormatException invalidFormatException
                    && User.Role.class.equals(invalidFormatException.getTargetType())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
