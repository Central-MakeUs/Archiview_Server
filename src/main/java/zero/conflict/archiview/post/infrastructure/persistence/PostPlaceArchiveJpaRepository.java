package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlaceArchive;

import java.util.List;
import java.util.UUID;

public interface PostPlaceArchiveJpaRepository extends JpaRepository<PostPlaceArchive, Long> {

    boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    List<PostPlaceArchive> findAllByArchiverIdOrderByCreatedAtDesc(UUID archiverId);
}
