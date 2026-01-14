package zero.conflict.archiview.global.error;

import org.springframework.http.HttpStatus;

public interface DomainErrorCode {

    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();

    default int getStatus() {
        return getHttpStatus().value();
    }
}