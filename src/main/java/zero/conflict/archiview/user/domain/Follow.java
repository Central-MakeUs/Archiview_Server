package zero.conflict.archiview.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
        name = "follow",
        uniqueConstraints = @UniqueConstraint(columnNames = { "archiver_id", "editor_id" })
)
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archiver_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID archiverId;

    @Column(name = "editor_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID editorId;

    public static Follow createOf(UUID archiverId, UUID editorId) {
        return Follow.builder()
                .archiverId(archiverId)
                .editorId(editorId)
                .build();
    }
}
