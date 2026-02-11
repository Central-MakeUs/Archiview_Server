package zero.conflict.archiview.post.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.archiver.command.PostReportCommandService;
import zero.conflict.archiview.post.application.port.out.PostPlaceReportRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostReportCommandService 테스트")
class PostReportCommandServiceTest {

    @InjectMocks
    private PostReportCommandService postReportCommandService;

    @Mock
    private PostPlaceRepository postPlaceRepository;

    @Mock
    private PostPlaceReportRepository postPlaceReportRepository;

    @Test
    @DisplayName("postPlace 신고 성공")
    void reportPostPlace_success() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(PostPlace.builder().id(postPlaceId).build()));
        given(postPlaceReportRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(false);

        postReportCommandService.reportPostPlace(archiverId, postPlaceId);

        verify(postPlaceReportRepository).save(any());
    }

    @Test
    @DisplayName("postPlace 중복 신고는 멱등 처리")
    void reportPostPlace_idempotent() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.of(PostPlace.builder().id(postPlaceId).build()));
        given(postPlaceReportRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)).willReturn(true);

        postReportCommandService.reportPostPlace(archiverId, postPlaceId);

        verify(postPlaceReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 postPlace 신고 시 예외")
    void reportPostPlace_notFound() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;
        given(postPlaceRepository.findById(postPlaceId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postReportCommandService.reportPostPlace(archiverId, postPlaceId))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(PostErrorCode.POST_PLACE_NOT_FOUND);
    }

    @Test
    @DisplayName("신고 취소는 멱등 처리")
    void cancelReportPostPlace_idempotent() {
        UUID archiverId = UUID.randomUUID();
        Long postPlaceId = 1L;

        postReportCommandService.cancelReportPostPlace(archiverId, postPlaceId);

        verify(postPlaceReportRepository).deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }
}
