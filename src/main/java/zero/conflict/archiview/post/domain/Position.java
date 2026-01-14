package zero.conflict.archiview.post.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

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
        validateLatitude(latitude);
        validateLongitude(longitude);
        return new Position(latitude, longitude);
    }

    private static void validateLatitude(BigDecimal latitude) {
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
            latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("위도는 -90 ~ 90 사이여야 합니다.");
        }
    }

    private static void validateLongitude(BigDecimal longitude) {
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
            longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("경도는 -180 ~ 180 사이여야 합니다.");
        }
    }
}