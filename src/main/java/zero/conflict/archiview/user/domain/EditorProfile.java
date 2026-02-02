package zero.conflict.archiview.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.util.UUID;

@Entity
@Table(name = "editor_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class EditorProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "introduction", nullable = false)
    private String introduction;

    @Column(nullable = false, unique = true)
    private String instagramId;

    @Column(nullable = false)
    private String instagramUrl;

    private String profileImageUrl;

    @Embedded
    private Hashtags hashtags;

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
}
