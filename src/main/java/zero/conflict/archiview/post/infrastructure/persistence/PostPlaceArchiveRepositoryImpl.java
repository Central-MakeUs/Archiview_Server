package zero.conflict.archiview.post.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostPlaceArchiveRepository;
import zero.conflict.archiview.post.domain.PostPlaceArchive;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostPlaceArchiveRepositoryImpl implements PostPlaceArchiveRepository {

    private final PostPlaceArchiveJpaRepository postPlaceArchiveJpaRepository;

    @Override
    public PostPlaceArchive save(PostPlaceArchive postPlaceArchive) {
        return postPlaceArchiveJpaRepository.save(postPlaceArchive);
    }

    @Override
    public boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId) {
        return postPlaceArchiveJpaRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }

    @Override
    public void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId) {
        postPlaceArchiveJpaRepository.deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }

    @Override
    public List<PostPlaceArchive> findAllByArchiverIdOrderByCreatedAtDesc(UUID archiverId) {
        return postPlaceArchiveJpaRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId);
    }

    @Override
    public List<PostPlaceArchive> findAllByArchiverIdAndPostPlaceIdIn(UUID archiverId, List<Long> postPlaceIds) {
        return postPlaceArchiveJpaRepository.findAllByArchiverIdAndPostPlaceIdIn(archiverId, postPlaceIds);
    }
}
