package zero.conflict.archiview.post.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.archiver.command.PostSaveCommandService;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceSaveRepository;
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
@DisplayName("PostSaveCommandService 테스트")
class PostSaveCommandServiceTest {

    @InjectMocks
    private PostSaveCommandService postSaveCommandService;

    @Mock
    private PostPlaceRepository postPlaceRepository;

    @Mock
    private PostPlaceSaveRepository postPlaceSaveRepository;

    @Test
    @DisplayName("postPlace 저장 성공")
    void savePostPlace_success() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        Long postPlaceId = 1L;
        PostPlace postPlace = PostPlace.builder()
                .id(postPlaceId)
                .editorId(editorId)
                .saveCount(0L)
                .build();
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(postPlace));
        given(postPlaceSaveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(false);

        postSaveCommandService.savePostPlace(archiverId, postPlaceId);

        verify(postPlaceSaveRepository).save(any());
        assertThat(postPlace.getSaveCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("postPlace 중복 저장은 멱등 처리")
    void savePostPlace_idempotent() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        PostPlace postPlace = PostPlace.builder()
                .id(postPlaceId)
                .editorId(UUID.randomUUID())
                .saveCount(3L)
                .build();
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(postPlace));
        given(postPlaceSaveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(true);

        postSaveCommandService.savePostPlace(archiverId, postPlaceId);

        verify(postPlaceSaveRepository, never()).save(any());
        assertThat(postPlace.getSaveCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("존재하지 않는 postPlace 저장 시 예외")
    void savePostPlace_notFound() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postSaveCommandService.savePostPlace(archiverId, postPlaceId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(PostErrorCode.POST_PLACE_NOT_FOUND);
    }

    @Test
    @DisplayName("postPlace 저장 해제 성공")
    void unsavePostPlace_success() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        PostPlace postPlace = PostPlace.builder()
                .id(postPlaceId)
                .editorId(UUID.randomUUID())
                .saveCount(2L)
                .build();
        given(postPlaceSaveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(true);
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(postPlace));

        postSaveCommandService.unsavePostPlace(archiverId, postPlaceId);

        verify(postPlaceSaveRepository).deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
        assertThat(postPlace.getSaveCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("저장하지 않은 postPlace 저장 해제는 멱등 처리")
    void unsavePostPlace_idempotent() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceSaveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(false);

        postSaveCommandService.unsavePostPlace(archiverId, postPlaceId);

        verify(postPlaceSaveRepository, never()).deleteByArchiverIdAndPostPlaceId(any(), any());
        verify(postPlaceRepository, never()).findById(any());
    }
}
