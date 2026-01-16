package zero.conflict.archiview.post.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long editorId;

    private String url;

    private String hashTag;

    private Boolean isDeleted;

    public static Post createOf(Long editorId, String url, String hashTag) {
        return Post.builder()
                .editorId(editorId)
                .url(url)
                .hashTag(hashTag)
                .isDeleted(false)
                .build();
    }

}
