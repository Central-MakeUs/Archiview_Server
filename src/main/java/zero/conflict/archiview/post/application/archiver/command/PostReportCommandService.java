package zero.conflict.archiview.post.application.archiver.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.PostPlaceReportRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.PostPlaceReport;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostReportCommandService {

    private final PostPlaceRepository postPlaceRepository;
    private final PostPlaceReportRepository postPlaceReportRepository;

    @Transactional
    public void reportPostPlace(UUID archiverId, Long postPlaceId) {
        var postPlace = postPlaceRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        if (archiverId.equals(postPlace.getEditorId())) {
            throw new DomainException(PostErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (postPlaceReportRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)) {
            return;
        }

        postPlaceReportRepository.save(PostPlaceReport.createOf(archiverId, postPlaceId));
    }

    @Transactional
    public void cancelReportPostPlace(UUID archiverId, Long postPlaceId) {
        postPlaceReportRepository.deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }
}
