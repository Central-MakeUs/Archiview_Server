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
public class PostPlaces extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long placeId;

    private String description;

    public static PostPlaces createOf(Long postId, Long placeId, String description) {
        return PostPlaces.builder()
                .postId(postId)
                .placeId(placeId)
                .description(description)
                .build();
    }

}
