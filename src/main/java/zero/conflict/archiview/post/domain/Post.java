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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long editorId;

    @Embedded
    private InstagramUrl url;

    @Embedded
    private HashTags hashTags;

    private Boolean isDeleted;

    public static Post createOf(Long editorId, String url, String hashTag) {
        return Post.builder()
                .editorId(editorId)
                .url(InstagramUrl.from(url))
                .hashTags(HashTags.from(hashTag))
                .isDeleted(false)
                .build();
    }

    public String getUrl() {
        return url.getValue();
    }

    public String getHashTag() {
        return hashTags.getValue();
    }

}
