package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.post.domain.PostPlaceSave;

import java.util.List;
import java.util.UUID;

public interface PostPlaceSaveJpaRepository extends JpaRepository<PostPlaceSave, Long> {

    boolean existsByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    void deleteByArchiverIdAndPostPlaceId(UUID archiverId, Long postPlaceId);

    List<PostPlaceSave> findAllByArchiverIdOrderByCreatedAtDesc(UUID archiverId);
}
