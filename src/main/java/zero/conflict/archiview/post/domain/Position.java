package zero.conflict.archiview.post.domain;

import jakarta.persistence.Embeddable;
import lombok.*;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Position {

    private BigDecimal latitude;  // 위도
    private BigDecimal longitude; // 경도

    public static Position of(BigDecimal latitude, BigDecimal longitude) {
        validateLatitudeWithinRange(latitude);
        validateLongitudeWithinRange(longitude);
        return new Position(latitude, longitude);
    }

    private static void validateLatitudeWithinRange(BigDecimal latitude) {
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
            latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new DomainException(PostErrorCode.INVALID_POSITION_LATITUDE);
        }
    }

    private static void validateLongitudeWithinRange(BigDecimal longitude) {
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
            longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new DomainException(PostErrorCode.INVALID_POSITION_LONGITUDE);
        }
    }
}
