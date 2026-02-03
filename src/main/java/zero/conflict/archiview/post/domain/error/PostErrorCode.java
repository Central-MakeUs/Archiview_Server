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
    INVALID_POSITION_LATITUDE(HttpStatus.BAD_REQUEST, "POST_INVALID_POSITION_LATITUDE",
            "error.post.invalid_position_latitude"),
    INVALID_POSITION_LONGITUDE(HttpStatus.BAD_REQUEST, "POST_INVALID_POSITION_LONGITUDE",
            "error.post.invalid_position_longitude"),
    INVALID_CATEGORY_ID(HttpStatus.BAD_REQUEST, "POST_INVALID_CATEGORY_ID", "error.post.invalid_category_id"),
    INVALID_INSTAGRAM_URL(HttpStatus.BAD_REQUEST, "POST_INVALID_INSTAGRAM_URL", "error.post.invalid_instagram_url"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "error.post.not_found"),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_FORBIDDEN", "error.post.forbidden"),
    POST_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_PLACE_NOT_FOUND", "error.post.place_not_found"),
    TOO_MANY_HASHTAGS(HttpStatus.BAD_REQUEST, "POST_TOO_MANY_HASHTAGS", "error.post.too_many_hashtags"),
    INVALID_BBOX_RANGE(HttpStatus.BAD_REQUEST, "POST_INVALID_BBOX_RANGE", "error.post.invalid_bbox_range"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
