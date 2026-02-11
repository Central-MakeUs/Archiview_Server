package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlaceReport;

import java.util.List;
import java.util.UUID;

public interface PostPlaceReportJpaRepository extends JpaRepository<PostPlaceReport, Long> {

    boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    List<PostPlaceReport> findAllByArchiverId(UUID archiverId);
}
