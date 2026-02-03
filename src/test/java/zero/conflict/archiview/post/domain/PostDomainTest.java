package zero.conflict.archiview.post.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zero.conflict.archiview.global.error.DomainException;
import error.PostErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostDomainTest {

    @Test
    @DisplayName("유효한 인스타그램 URL과 해시태그로 Post를 생성할 수 있다")
    void createPost_success() {
        // given
        java.util.UUID editorId = java.util.UUID.randomUUID();
        String url = "https://www.instagram.com/p/DBU0yXOz_A-/";
        String hashTag = "#성수 #카페";

        // when
        Post post = Post.createOf(editorId, url, hashTag);

        // then
        assertThat(post.getUrl()).isEqualTo(url);
        assertThat(post.getHashTag()).isEqualTo(hashTag);
    }

    @Test
    @DisplayName("잘못된 형식의 인스타그램 URL은 예외를 발생시킨다")
    void createPost_invalidUrl_throwsException() {
        // given
        java.util.UUID editorId = java.util.UUID.randomUUID();
        String invalidUrl = "https://wrong-url.com";
        String hashTag = "#성수";

        // when & then
        assertThatThrownBy(() -> Post.createOf(editorId, invalidUrl, hashTag))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", PostErrorCode.INVALID_INSTAGRAM_URL);
    }

    @Test
    @DisplayName("해시태그가 3개를 초과하면 예외를 발생시킨다")
    void createPost_tooManyHashTags_throwsException() {
        // given
        java.util.UUID editorId = java.util.UUID.randomUUID();
        String url = "https://www.instagram.com/p/DBU0yXOz_A-/";
        String tooManyTags = "#하나 #둘 #셋 #넷";

        // when & then
        assertThatThrownBy(() -> Post.createOf(editorId, url, tooManyTags))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", PostErrorCode.TOO_MANY_HASHTAGS);
    }
}
