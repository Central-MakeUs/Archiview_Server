package zero.conflict.archiview.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtags {

    @Column(name = "hashtag_1", nullable = false)
    private String first;

    @Column(name = "hashtag_2", nullable = false)
    private String second;

    private Hashtags(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public static Hashtags of(String first, String second) {
        if (isBlank(first) || isBlank(second)) {
            throw new DomainException(UserErrorCode.INVALID_HASHTAG);
        }
        return new Hashtags(first.trim(), second.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
