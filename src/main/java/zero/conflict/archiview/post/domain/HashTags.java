package zero.conflict.archiview.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class HashTags {

    private static final int MAX_TAG_COUNT = 3;

    @Column(name = "hash_tag")
    private String value;

    public HashTags(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        long count = Arrays.stream(value.trim().split("\\s+"))
                .filter(tag -> !tag.isBlank())
                .count();

        if (count > MAX_TAG_COUNT) {
            throw new DomainException(PostErrorCode.TOO_MANY_HASHTAGS);
        }
    }

    public List<String> getTagList() {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.trim().split("\\s+"))
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toList());
    }

    public static HashTags from(String value) {
        return new HashTags(value);
    }
}
