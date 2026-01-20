package zero.conflict.archiview.post.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Position {

    private Double latitude;  // 위도
    private Double longitude; // 경도

    public static Position of(Double latitude, Double longitude) {
        validateLatitudeWithinRange(latitude);
        validateLongitudeWithinRange(longitude);
        return new Position(latitude, longitude);
    }

    private static void validateLatitudeWithinRange(Double latitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new DomainException(PostErrorCode.INVALID_POSITION_LATITUDE);
        }
    }

    private static void validateLongitudeWithinRange(Double longitude) {
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new DomainException(PostErrorCode.INVALID_POSITION_LONGITUDE);
        }
    }
}
