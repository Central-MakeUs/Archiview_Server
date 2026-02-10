package zero.conflict.archiview.user.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.EditorBlockRepository;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.EditorBlock;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.BlockDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockQueryService 테스트")
class BlockQueryServiceTest {

    @InjectMocks
    private BlockQueryService blockQueryService;

    @Mock
    private EditorBlockRepository editorBlockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EditorProfileRepository editorProfileRepository;

    @Test
    @DisplayName("내 차단 에디터 목록 조회 성공")
    void getMyBlockedEditors_success() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));

        EditorBlock block = EditorBlock.builder()
                .id(1L)
                .archiverId(archiverId)
                .editorId(editorId)
                .createdAt(LocalDateTime.of(2026, 2, 10, 9, 0, 0))
                .build();
        given(editorBlockRepository.findAllByArchiverId(archiverId)).willReturn(List.of(block));

        EditorProfile profile = EditorProfile.builder()
                .user(User.builder().id(editorId).build())
                .nickname("editor")
                .instagramId("editor_id")
                .instagramUrl("https://www.instagram.com/editor_id")
                .introduction("소개")
                .build();
        given(editorProfileRepository.findAllByUserIds(List.of(editorId))).willReturn(List.of(profile));

        BlockDto.ListResponse response = blockQueryService.getMyBlockedEditors(archiverId);

        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getEditors()).hasSize(1);
        assertThat(response.getEditors().get(0).getEditorId()).isEqualTo(editorId);
        assertThat(response.getEditors().get(0).getBlockedAt()).isEqualTo(LocalDateTime.of(2026, 2, 10, 9, 0, 0));
    }

    @Test
    @DisplayName("내 차단 에디터 목록이 비어있으면 empty 반환")
    void getMyBlockedEditors_empty() {
        UUID archiverId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(editorBlockRepository.findAllByArchiverId(archiverId)).willReturn(List.of());

        BlockDto.ListResponse response = blockQueryService.getMyBlockedEditors(archiverId);

        assertThat(response.getTotalCount()).isEqualTo(0);
        assertThat(response.getEditors()).isEmpty();
    }

    @Test
    @DisplayName("아카이버가 없으면 예외")
    void getMyBlockedEditors_archiverNotFound() {
        UUID archiverId = UUID.randomUUID();
        given(userRepository.findById(archiverId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> blockQueryService.getMyBlockedEditors(archiverId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("아카이버 역할이 아니면 예외")
    void getMyBlockedEditors_invalidRole() {
        UUID archiverId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.EDITOR).build()));

        assertThatThrownBy(() -> blockQueryService.getMyBlockedEditors(archiverId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_FOLLOWER_ROLE);
    }
}
