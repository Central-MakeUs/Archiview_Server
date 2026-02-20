package zero.conflict.archiview.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("탈퇴 이메일에 기존 이메일이 포함된다")
    void markDeleted_includesOriginalEmail() {
        User user = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .email("user@example.com")
                .name("테스트")
                .provider(User.OAuthProvider.APPLE)
                .providerId("apple-sub-1")
                .role(User.Role.EDITOR)
                .build();

        user.markDeleted(LocalDateTime.of(2026, 2, 20, 10, 0, 0));

        assertThat(user.getEmail()).contains("user@example.com");
        assertThat(user.getEmail()).startsWith("withdrawn+");
        assertThat(user.getEmail()).endsWith("@archiview.local");
    }

    @Test
    @DisplayName("탈퇴 이메일이 255자를 넘으면 자동 축약과 해시를 적용한다")
    void markDeleted_truncatesWhenTooLong() {
        String longLocalPart = "a".repeat(230);
        User user = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .email(longLocalPart + "@example.com")
                .name("테스트")
                .provider(User.OAuthProvider.GOOGLE)
                .providerId("google-sub-2")
                .role(User.Role.ARCHIVER)
                .build();

        user.markDeleted(LocalDateTime.of(2026, 2, 20, 10, 0, 0));

        assertThat(user.getEmail().length()).isLessThanOrEqualTo(255);
        assertThat(user.getEmail()).contains("..+h");
        assertThat(user.getEmail()).endsWith("@archiview.local");
    }

    @Test
    @DisplayName("이미 탈퇴한 회원은 재호출해도 탈퇴 식별값이 유지된다")
    void markDeleted_isIdempotent() {
        User user = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000003"))
                .email("first@example.com")
                .name("테스트")
                .provider(User.OAuthProvider.KAKAO)
                .providerId("kakao-sub-3")
                .role(User.Role.GUEST)
                .build();

        user.markDeleted(LocalDateTime.of(2026, 2, 20, 10, 0, 0));
        String firstDeletedEmail = user.getEmail();
        String firstProviderId = user.getProviderId();

        user.markDeleted(LocalDateTime.of(2026, 2, 21, 10, 0, 0));

        assertThat(user.getEmail()).isEqualTo(firstDeletedEmail);
        assertThat(user.getProviderId()).isEqualTo(firstProviderId);
    }
}
