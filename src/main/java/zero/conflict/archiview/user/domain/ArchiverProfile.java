package zero.conflict.archiview.user.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.util.UUID;

@Entity
@Table(name = "archiver_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class ArchiverProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    public static ArchiverProfile createOf(User user, String nickname) {
        return ArchiverProfile.builder()
                .user(user)
                .nickname(nickname)
                .build();
    }
}
