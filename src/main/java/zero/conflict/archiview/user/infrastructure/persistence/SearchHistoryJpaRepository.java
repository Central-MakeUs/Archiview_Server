package zero.conflict.archiview.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import zero.conflict.archiview.user.domain.SearchHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, Long> {

    Optional<SearchHistory> findByArchiverIdAndKeywordNormalized(UUID archiverId, String keywordNormalized);

    List<SearchHistory> findAllByArchiverIdOrderByLastModifiedAtDesc(UUID archiverId);

    void deleteByIdAndArchiverId(Long id, UUID archiverId);
}
