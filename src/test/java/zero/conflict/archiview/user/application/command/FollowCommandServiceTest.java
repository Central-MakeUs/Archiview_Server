package zero.conflict.archiview.user.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.archiver.command.FollowCommandService;
import zero.conflict.archiview.user.application.port.out.FollowRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowCommandService 테스트")
class FollowCommandServiceTest {

    @InjectMocks
    private FollowCommandService followCommandService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("팔로우 성공")
    void follow_success() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(userRepository.findById(editorId))
                .willReturn(Optional.of(User.builder().id(editorId).role(User.Role.EDITOR).build()));
        given(followRepository.existsByArchiverIdAndEditorId(archiverId, editorId)).willReturn(false);

        followCommandService.follow(archiverId, editorId);

        verify(followRepository).save(any());
    }

    @Test
    @DisplayName("자기 자신 팔로우는 예외")
    void follow_self_throwsException() {
        UUID sameId = UUID.randomUUID();

        assertThatThrownBy(() -> followCommandService.follow(sameId, sameId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.SELF_FOLLOW_NOT_ALLOWED);
    }

    @Test
    @DisplayName("팔로우 대상이 에디터가 아니면 예외")
    void follow_invalidFolloweeRole() {
        UUID archiverId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(userRepository.findById(targetId))
                .willReturn(Optional.of(User.builder().id(targetId).role(User.Role.ARCHIVER).build()));

        assertThatThrownBy(() -> followCommandService.follow(archiverId, targetId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_FOLLOWEE_ROLE);
    }

    @Test
    @DisplayName("중복 팔로우는 예외")
    void follow_duplicate_throwsException() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(userRepository.findById(editorId))
                .willReturn(Optional.of(User.builder().id(editorId).role(User.Role.EDITOR).build()));
        given(followRepository.existsByArchiverIdAndEditorId(archiverId, editorId)).willReturn(true);

        assertThatThrownBy(() -> followCommandService.follow(archiverId, editorId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.FOLLOW_ALREADY_EXISTS);
        verify(followRepository, never()).save(any());
    }
}
