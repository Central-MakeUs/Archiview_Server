package zero.conflict.archiview.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class HashTags {

    @Column(name = "hash_tag_1", nullable = false)
    private String primaryTag;

    @Column(name = "hash_tag_2", nullable = false)
    private String secondaryTag;

    private HashTags(String primaryTag, String secondaryTag) {
        this.primaryTag = primaryTag;
        this.secondaryTag = secondaryTag;
    }

    public static HashTags of(String primaryTag, String secondaryTag) {
        if (isBlank(primaryTag) || isBlank(secondaryTag)) {
            throw new DomainException(PostErrorCode.INVALID_HASHTAG);
        }
        return new HashTags(primaryTag.trim(), secondaryTag.trim());
    }

    public static HashTags from(List<String> tags) {
        if (tags == null || tags.size() != 2) {
            throw new DomainException(PostErrorCode.INVALID_HASHTAG);
        }
        return of(tags.get(0), tags.get(1));
    }

    public List<String> asList() {
        return List.of(primaryTag, secondaryTag);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
