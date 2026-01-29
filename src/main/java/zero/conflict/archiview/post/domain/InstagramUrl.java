package zero.conflict.archiview.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class InstagramUrl {

    private static final String INSTAGRAM_PREFIX = "https://www.instagram.com/";

    @Column(name = "url", nullable = false)
    private String value;

    public InstagramUrl(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || !value.startsWith(INSTAGRAM_PREFIX)) {
            throw new DomainException(PostErrorCode.INVALID_INSTAGRAM_URL);
        }
    }

    public static InstagramUrl from(String value) {
        return new InstagramUrl(value);
    }
}
