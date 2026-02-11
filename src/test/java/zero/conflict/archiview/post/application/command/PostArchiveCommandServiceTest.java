package zero.conflict.archiview.post.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.archiver.command.PostArchiveCommandService;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceArchiveRepository;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostArchiveCommandService 테스트")
class PostArchiveCommandServiceTest {

    @InjectMocks
    private PostArchiveCommandService postArchiveCommandService;

    @Mock
    private PostPlaceRepository postPlaceRepository;

    @Mock
    private PostPlaceArchiveRepository postPlaceArchiveRepository;

    @Test
    @DisplayName("postPlace 아카이브 성공")
    void archivePostPlace_success() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        Long postPlaceId = 1L;
        PostPlace postPlace = PostPlace.builder()
                .id(postPlaceId)
                .editorId(editorId)
                .saveCount(0L)
                .build();
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(postPlace));
        given(postPlaceArchiveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(false);

        postArchiveCommandService.archivePostPlace(archiverId, postPlaceId);

        verify(postPlaceArchiveRepository).save(any());
        assertThat(postPlace.getSaveCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("postPlace 중복 아카이브는 멱등 처리")
    void archivePostPlace_idempotent() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        PostPlace postPlace = PostPlace.builder()
                .id(postPlaceId)
                .editorId(UUID.randomUUID())
                .saveCount(3L)
                .build();
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(postPlace));
        given(postPlaceArchiveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(true);

        postArchiveCommandService.archivePostPlace(archiverId, postPlaceId);

        verify(postPlaceArchiveRepository, never()).save(any());
        assertThat(postPlace.getSaveCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("존재하지 않는 postPlace 아카이브 시 예외")
    void archivePostPlace_notFound() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postArchiveCommandService.archivePostPlace(archiverId, postPlaceId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(PostErrorCode.POST_PLACE_NOT_FOUND);
    }

    @Test
    @DisplayName("postPlace 아카이브 해제 성공")
    void unarchivePostPlace_success() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        PostPlace postPlace = PostPlace.builder()
                .id(postPlaceId)
                .editorId(UUID.randomUUID())
                .saveCount(2L)
                .build();
        given(postPlaceArchiveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(true);
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(postPlace));

        postArchiveCommandService.unarchivePostPlace(archiverId, postPlaceId);

        verify(postPlaceArchiveRepository).deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
        assertThat(postPlace.getSaveCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("아카이브하지 않은 postPlace 아카이브 해제는 멱등 처리")
    void unarchivePostPlace_idempotent() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceArchiveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(false);

        postArchiveCommandService.unarchivePostPlace(archiverId, postPlaceId);

        verify(postPlaceArchiveRepository, never()).deleteByArchiverIdAndPostPlaceId(any(), any());
        verify(postPlaceRepository, never()).findById(any());
    }
}
