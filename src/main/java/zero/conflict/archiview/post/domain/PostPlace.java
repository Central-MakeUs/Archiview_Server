package zero.conflict.archiview.post.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostPlace extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long placeId;

    private Long editorId;

    private String description;

    public static PostPlace createOf(Long postId, Long placeId, String description, Long editorId) {
        return PostPlace.builder()
                .postId(postId)
                .placeId(placeId)
                .description(description)
                .editorId(editorId)
                .build();
    }

}
