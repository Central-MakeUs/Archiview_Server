package zero.conflict.archiview.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "editor_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class EditorProfile extends BaseTimeEntity {

    private static final DateTimeFormatter DELETED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "introduction", nullable = false)
    private String introduction;

    @Column(nullable = false, unique = true)
    private String instagramId;

    @Column(nullable = false)
    private String instagramUrl;

    @Column(length = 1000)
    private String profileImageUrl;

    @Embedded
    private Hashtags hashtags;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static EditorProfile createOf(User user, String nickname, String introduction, String instagramId,
            String instagramUrl, String profileImageUrl, Hashtags hashtags) {
        return EditorProfile.builder()
                .user(user)
                .nickname(nickname)
                .introduction(introduction)
                .instagramId(instagramId)
                .instagramUrl(instagramUrl)
                .profileImageUrl(profileImageUrl)
                .hashtags(hashtags)
                .build();
    }

    public void update(String nickname, String introduction, String instagramId, String instagramUrl,
            String profileImageUrl, Hashtags hashtags) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.instagramId = instagramId;
        this.instagramUrl = instagramUrl;
        this.profileImageUrl = profileImageUrl;
        this.hashtags = hashtags;
    }

    public java.util.UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        if (this.deletedAt == null) {
            LocalDateTime now = deletedAt != null ? deletedAt : LocalDateTime.now();
            this.deletedAt = now;

            String deletedAtText = now.format(DELETED_AT_FORMATTER);
            String nicknameBase = this.nickname != null && !this.nickname.isBlank() ? this.nickname : "deleted_nick";
            String instagramIdBase = this.instagramId != null && !this.instagramId.isBlank()
                    ? this.instagramId
                    : "deleted_insta";
            this.nickname = nicknameBase + "_deleted_" + deletedAtText;
            this.instagramId = instagramIdBase + "_deleted_" + deletedAtText;
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
