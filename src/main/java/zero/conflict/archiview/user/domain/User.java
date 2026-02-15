package zero.conflict.archiview.user.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(nullable = false, unique = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum OAuthProvider {
        GOOGLE, APPLE, KAKAO
    }

    public enum Role {
        GUEST, ARCHIVER, EDITOR
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void assignRole(Role role) {
        this.role = role;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        if (this.deletedAt != null) {
            return;
        }
        LocalDateTime now = deletedAt != null ? deletedAt : LocalDateTime.now();
        this.deletedAt = now;

        long epochMillis = now.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        String suffix = (this.id != null ? this.id.toString() : "unknown") + "-" + epochMillis;
        this.email = "withdrawn+" + suffix + "@archiview.local";
        this.providerId = "withdrawn:" + suffix;
        this.name = "탈퇴한 회원";
        this.role = Role.GUEST;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
