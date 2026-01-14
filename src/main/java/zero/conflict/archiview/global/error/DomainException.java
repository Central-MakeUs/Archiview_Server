package zero.conflict.archiview.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DomainException extends RuntimeException {

    private final DomainErrorCode errorCode;

}