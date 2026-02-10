package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostPlaceReportRepository;
import zero.conflict.archiview.post.domain.PostPlaceReport;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostPlaceReportRepositoryImpl implements PostPlaceReportRepository {

    private final PostPlaceReportJpaRepository postPlaceReportJpaRepository;

    @Override
    public PostPlaceReport save(PostPlaceReport report) {
        return postPlaceReportJpaRepository.save(report);
    }

    @Override
    public boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId) {
        return postPlaceReportJpaRepository.existsByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }

    @Override
    public void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId) {
        postPlaceReportJpaRepository.deleteByArchiverIdAndPostPlaceId(archiverId, postPlaceId);
    }

    @Override
    public List<PostPlaceReport> findAllByArchiverId(UUID archiverId) {
        return postPlaceReportJpaRepository.findAllByArchiverId(archiverId);
    }
}
