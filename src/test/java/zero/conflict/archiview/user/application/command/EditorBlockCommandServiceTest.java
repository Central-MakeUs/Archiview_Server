package zero.conflict.archiview.user.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.EditorBlockRepository;
import zero.conflict.archiview.user.application.port.UserRepository;
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
@DisplayName("EditorBlockCommandService 테스트")
class EditorBlockCommandServiceTest {

    @InjectMocks
    private EditorBlockCommandService editorBlockCommandService;

    @Mock
    private EditorBlockRepository editorBlockRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("에디터 차단 성공")
    void blockEditor_success() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId)).willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(userRepository.findById(editorId)).willReturn(Optional.of(User.builder().id(editorId).role(User.Role.EDITOR).build()));
        given(editorBlockRepository.existsByArchiverIdAndEditorId(archiverId, editorId)).willReturn(false);

        editorBlockCommandService.blockEditor(archiverId, editorId);

        verify(editorBlockRepository).save(any());
    }

    @Test
    @DisplayName("중복 차단은 멱등 처리")
    void blockEditor_idempotent() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId)).willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(userRepository.findById(editorId)).willReturn(Optional.of(User.builder().id(editorId).role(User.Role.EDITOR).build()));
        given(editorBlockRepository.existsByArchiverIdAndEditorId(archiverId, editorId)).willReturn(true);

        editorBlockCommandService.blockEditor(archiverId, editorId);

        verify(editorBlockRepository, never()).save(any());
    }

    @Test
    @DisplayName("차단 대상이 에디터가 아니면 예외")
    void blockEditor_invalidTargetRole() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId)).willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(userRepository.findById(editorId)).willReturn(Optional.of(User.builder().id(editorId).role(User.Role.ARCHIVER).build()));

        assertThatThrownBy(() -> editorBlockCommandService.blockEditor(archiverId, editorId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_FOLLOWEE_ROLE);
    }

    @Test
    @DisplayName("차단 해제는 멱등 처리")
    void unblockEditor_idempotent() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();

        editorBlockCommandService.unblockEditor(archiverId, editorId);

        verify(editorBlockRepository).deleteByArchiverIdAndEditorId(archiverId, editorId);
    }
}
