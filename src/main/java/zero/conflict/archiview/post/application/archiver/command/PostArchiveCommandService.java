package zero.conflict.archiview.post.application.archiver.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceArchiveRepository;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceArchive;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostArchiveCommandService {

    private final PostPlaceRepository postPlaceRepository;
    private final PostPlaceArchiveRepository postPlaceArchiveRepository;

    @Transactional
    public void archivePostPlace(UUID archiverId, Long postPlaceId) {
        PostPlace postPlace = postPlaceRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        if (postPlaceArchiveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)) {
            return;
        }

        postPlaceArchiveRepository.save(PostPlaceArchive.createOf(archiverId, postPlaceId));
        postPlace.increaseSaveCount(archiverId);
    }

    @Transactional
    public void unarchivePostPlace(UUID archiverId, Long postPlaceId) {
        if (!postPlaceArchiveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)) {
            return;
        }

        postPlaceArchiveRepository.deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
        postPlaceRepository.findById(postPlaceId)
                .ifPresent(postPlace -> postPlace.decreaseSaveCount(archiverId));
    }
}
