package zero.conflict.archiview.user.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.editor.query.EditorProfileQueryService;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Hashtags;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EditorProfileQueryServiceTest {

    @Mock
    private EditorProfileRepository editorProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EditorProfileQueryService editorProfileQueryService;

    @Test
    @DisplayName("활성 에디터는 기존 프로필을 반환한다")
    void getEditorProfile_active_success() {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        User user = User.builder()
                .id(editorId)
                .email("editor@archiview.com")
                .name("editor")
                .provider(User.OAuthProvider.KAKAO)
                .providerId("provider-id")
                .role(User.Role.EDITOR)
                .build();
        EditorProfile profile = EditorProfile.builder()
                .id(1L)
                .user(user)
                .nickname("맛집탐방가")
                .introduction("소개")
                .instagramId("editor_insta")
                .instagramUrl("https://www.instagram.com/editor_insta")
                .hashtags(Hashtags.of("#A", "#B"))
                .build();

        given(editorProfileRepository.findByUserId(editorId)).willReturn(Optional.of(profile));

        EditorProfileDto.Response response = editorProfileQueryService.getEditorProfile(editorId);

        assertThat(response.isWithdrawn()).isFalse();
        assertThat(response.getNickname()).isEqualTo("맛집탐방가");
    }

    @Test
    @DisplayName("탈퇴한 에디터는 withdrawn 응답을 반환한다")
    void getEditorProfile_withdrawn_returnsWithdrawnResponse() {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000202");
        User deletedUser = User.builder()
                .id(editorId)
                .email("deleted@archiview.com")
                .name("deleted")
                .provider(User.OAuthProvider.KAKAO)
                .providerId("provider-id")
                .role(User.Role.GUEST)
                .deletedAt(LocalDateTime.now())
                .build();

        given(editorProfileRepository.findByUserId(editorId)).willReturn(Optional.empty());
        given(userRepository.findByIdIncludingDeleted(editorId)).willReturn(Optional.of(deletedUser));

        EditorProfileDto.Response response = editorProfileQueryService.getEditorProfile(editorId);

        assertThat(response.isWithdrawn()).isTrue();
        assertThat(response.getWithdrawnMessage()).isEqualTo("탈퇴한 회원");
    }

    @Test
    @DisplayName("에디터가 없고 탈퇴 사용자도 아니면 EDITOR_PROFILE_NOT_FOUND 예외를 반환한다")
    void getEditorProfile_notFound_throwsException() {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000203");

        given(editorProfileRepository.findByUserId(editorId)).willReturn(Optional.empty());
        given(userRepository.findByIdIncludingDeleted(editorId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> editorProfileQueryService.getEditorProfile(editorId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.EDITOR_PROFILE_NOT_FOUND);
    }
}
