package zero.conflict.archiview.post.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID editorId;

    @Embedded
    private InstagramUrl url;

    @Embedded
    private HashTags hashTags;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    public static Post createOf(UUID editorId, String url, List<String> hashTags) {
        return Post.builder()
                .editorId(editorId)
                .url(InstagramUrl.from(url))
                .hashTags(HashTags.from(hashTags))
                .isDeleted(false)
                .deletedAt(null)
                .build();
    }

    public String getUrl() {
        return url.getValue();
    }

    public List<String> getHashTags() {
        return hashTags.asList();
    }

    public void update(String url, List<String> hashTags) {
        this.url = InstagramUrl.from(url);
        this.hashTags = HashTags.from(hashTags);
    }

    public void markDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void markDeleted(LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

}
