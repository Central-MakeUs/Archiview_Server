package zero.conflict.archiview.user.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final String WITHDRAWN_EMAIL_PREFIX = "withdrawn+";
    private static final String WITHDRAWN_EMAIL_DOMAIN = "@archiview.local";
    private static final String WITHDRAWN_HASH_PREFIX = "+h";
    private static final int WITHDRAWN_HASH_LENGTH = 10;

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
        this.email = buildWithdrawnEmail(this.email, suffix);
        this.providerId = "withdrawn:" + suffix;
        this.name = "탈퇴한 회원";
        this.role = Role.GUEST;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    private String buildWithdrawnEmail(String originalEmail, String suffix) {
        String safeOriginalEmail = originalEmail == null ? "unknown" : originalEmail;
        String fullCandidate = WITHDRAWN_EMAIL_PREFIX + safeOriginalEmail + "+" + suffix + WITHDRAWN_EMAIL_DOMAIN;
        if (fullCandidate.length() <= EMAIL_MAX_LENGTH) {
            return fullCandidate;
        }

        String hash = sha256Hex(safeOriginalEmail);
        String hashPart = WITHDRAWN_HASH_PREFIX + hash.substring(0, WITHDRAWN_HASH_LENGTH);
        String fixedPart = WITHDRAWN_EMAIL_PREFIX + ".." + hashPart + "+" + suffix + WITHDRAWN_EMAIL_DOMAIN;
        int maxOriginalLength = EMAIL_MAX_LENGTH - fixedPart.length();
        if (maxOriginalLength < 0) {
            String truncatedSuffix = suffix.substring(Math.max(0, suffix.length() - 32));
            return WITHDRAWN_EMAIL_PREFIX + "h" + hash.substring(0, WITHDRAWN_HASH_LENGTH) + "+"
                    + truncatedSuffix + WITHDRAWN_EMAIL_DOMAIN;
        }

        String truncatedOriginal = safeOriginalEmail.substring(0, Math.min(maxOriginalLength, safeOriginalEmail.length()));
        return WITHDRAWN_EMAIL_PREFIX + truncatedOriginal + ".." + hashPart + "+" + suffix + WITHDRAWN_EMAIL_DOMAIN;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
