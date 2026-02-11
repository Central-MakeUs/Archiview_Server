package zero.conflict.archiview.post.application.archiver.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceSaveRepository;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceSave;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostSaveCommandService {

    private final PostPlaceRepository postPlaceRepository;
    private final PostPlaceSaveRepository postPlaceSaveRepository;

    @Transactional
    public void savePostPlace(UUID archiverId, Long postPlaceId) {
        PostPlace postPlace = postPlaceRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        if (postPlaceSaveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)) {
            return;
        }

        postPlaceSaveRepository.save(PostPlaceSave.createOf(archiverId, postPlaceId));
        postPlace.increaseSaveCount(archiverId);
    }

    @Transactional
    public void unsavePostPlace(UUID archiverId, Long postPlaceId) {
        if (!postPlaceSaveRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId)) {
            return;
        }

        postPlaceSaveRepository.deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
        postPlaceRepository.findById(postPlaceId)
                .ifPresent(postPlace -> postPlace.decreaseSaveCount(archiverId));
    }
}
