package zero.conflict.archiview.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class InstagramUrl {

    private static final Pattern INSTAGRAM_URL_PATTERN =
            Pattern.compile("^https://(www\\.)?instagram\\.com/[^\\s]+$");

    @Column(name = "url", nullable = false)
    private String value;

    public InstagramUrl(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || !INSTAGRAM_URL_PATTERN.matcher(value).matches()) {
            throw new DomainException(PostErrorCode.INVALID_INSTAGRAM_URL);
        }
    }

    public static InstagramUrl from(String value) {
        return new InstagramUrl(value);
    }
}
