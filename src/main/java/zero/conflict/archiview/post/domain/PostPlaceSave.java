package zero.conflict.archiview.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "post_place_save",
        uniqueConstraints = @UniqueConstraint(columnNames = { "archiver_id", "post_place_id" }))
public class PostPlaceSave extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archiver_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID archiverId;

    @Column(name = "post_place_id", nullable = false)
    private Long postPlaceId;

    public static PostPlaceSave createOf(UUID archiverId, Long postPlaceId) {
        return PostPlaceSave.builder()
                .archiverId(archiverId)
                .postPlaceId(postPlaceId)
                .build();
    }
}
