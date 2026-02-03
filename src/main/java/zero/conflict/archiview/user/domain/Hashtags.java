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
    private String primaryTag;

    @Column(name = "hashtag_2", nullable = false)
    private String secondaryTag;

    private Hashtags(String primaryTag, String secondaryTag) {
        this.primaryTag = primaryTag;
        this.secondaryTag = secondaryTag;
    }

    public static Hashtags of(String primaryTag, String secondaryTag) {
        if (isBlank(primaryTag) || isBlank(secondaryTag)) {
            throw new DomainException(UserErrorCode.INVALID_HASHTAG);
        }
        return new Hashtags(primaryTag.trim(), secondaryTag.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
