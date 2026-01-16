package zero.conflict.archiview.post.domain.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import zero.conflict.archiview.global.error.DomainErrorCode;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements DomainErrorCode {

    INVALID_PLACE_NAME("POST_INVALID_PLACE_NAME", "error.post.invalid_place_name", HttpStatus.BAD_REQUEST),
    INVALID_PLACE_ADDRESS("POST_INVALID_PLACE_ADDRESS", "error.post.invalid_place_address", HttpStatus.BAD_REQUEST)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
