package zero.conflict.archiview.post.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostPlaceSaveRepository;
import zero.conflict.archiview.post.domain.PostPlaceSave;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostPlaceSaveRepositoryImpl implements PostPlaceSaveRepository {

    private final PostPlaceSaveJpaRepository postPlaceSaveJpaRepository;

    @Override
    public PostPlaceSave save(PostPlaceSave postPlaceSave) {
        return postPlaceSaveJpaRepository.save(postPlaceSave);
    }

    @Override
    public boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId) {
        return postPlaceSaveJpaRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }

    @Override
    public void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId) {
        postPlaceSaveJpaRepository.deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }

    @Override
    public List<PostPlaceSave> findAllByArchiverIdOrderByCreatedAtDesc(UUID archiverId) {
        return postPlaceSaveJpaRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId);
    }
}
