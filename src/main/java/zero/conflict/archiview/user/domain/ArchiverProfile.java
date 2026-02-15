package zero.conflict.archiview.user.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "archiver_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class ArchiverProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static ArchiverProfile createOf(User user, String nickname) {
        return ArchiverProfile.builder()
                .user(user)
                .nickname(nickname)
                .build();
    }

    public void markDeleted(LocalDateTime deletedAt) {
        if (this.deletedAt == null) {
            this.deletedAt = deletedAt != null ? deletedAt : LocalDateTime.now();
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
