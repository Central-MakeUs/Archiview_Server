package zero.conflict.archiview.post.domain.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import zero.conflict.archiview.global.error.DomainErrorCode;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements DomainErrorCode {

    INVALID_PLACE_NAME(HttpStatus.BAD_REQUEST, "POST_INVALID_PLACE_NAME", "error.post.invalid_place_name"),
    INVALID_PLACE_ADDRESS(HttpStatus.BAD_REQUEST, "POST_INVALID_PLACE_ADDRESS", "error.post.invalid_place_address"),
    INVALID_POSITION_LATITUDE(HttpStatus.BAD_REQUEST, "POST_INVALID_POSITION_LATITUDE", "error.post.invalid_position_latitude"),
    INVALID_POSITION_LONGITUDE(HttpStatus.BAD_REQUEST, "POST_INVALID_POSITION_LONGITUDE", "error.post.invalid_position_longitude"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
