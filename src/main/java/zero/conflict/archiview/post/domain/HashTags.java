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
import java.util.ArrayList;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class HashTags {

    @Column(name = "hash_tag_1", nullable = false)
    private String primaryTag;

    @Column(name = "hash_tag_2")
    private String secondaryTag;

    @Column(name = "hash_tag_3")
    private String tertiaryTag;

    private HashTags(String primaryTag, String secondaryTag, String tertiaryTag) {
        this.primaryTag = primaryTag;
        this.secondaryTag = secondaryTag;
        this.tertiaryTag = tertiaryTag;
    }

    public static HashTags of(String primaryTag, String secondaryTag) {
        return of(primaryTag, secondaryTag, null);
    }

    public static HashTags of(String primaryTag, String secondaryTag, String tertiaryTag) {
        if (isBlank(primaryTag) || isBlankIfPresent(secondaryTag) || isBlankIfPresent(tertiaryTag)) {
            throw new DomainException(PostErrorCode.INVALID_HASHTAG);
        }
        return new HashTags(
                primaryTag.trim(),
                normalizeOptionalTag(secondaryTag),
                normalizeOptionalTag(tertiaryTag));
    }

    public static HashTags from(List<String> tags) {
        if (tags == null || tags.size() < 1 || tags.size() > 3) {
            throw new DomainException(PostErrorCode.INVALID_HASHTAG);
        }

        List<String> normalized = tags.stream()
                .map(tag -> tag == null ? null : tag.trim())
                .toList();

        if (normalized.stream().anyMatch(HashTags::isBlank)) {
            throw new DomainException(PostErrorCode.INVALID_HASHTAG);
        }

        String secondary = normalized.size() >= 2 ? normalized.get(1) : null;
        String tertiary = normalized.size() == 3 ? normalized.get(2) : null;
        return of(normalized.get(0), secondary, tertiary);
    }

    public List<String> asList() {
        List<String> tags = new ArrayList<>();
        tags.add(primaryTag);
        if (secondaryTag != null) {
            tags.add(secondaryTag);
        }
        if (tertiaryTag != null) {
            tags.add(tertiaryTag);
        }
        return List.copyOf(tags);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isBlankIfPresent(String value) {
        return value != null && value.trim().isEmpty();
    }

    private static String normalizeOptionalTag(String value) {
        return value == null ? null : value.trim();
    }
}
