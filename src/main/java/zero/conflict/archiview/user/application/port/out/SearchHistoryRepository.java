package zero.conflict.archiview.user.application.port.out;

import zero.conflict.archiview.user.domain.SearchHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchHistoryRepository {

    SearchHistory save(SearchHistory searchHistory);

    Optional<SearchHistory> findByArchiverIdAndKeywordNormalized(UUID archiverId, String keywordNormalized);

    List<SearchHistory> findAllByArchiverIdOrderByLastModifiedAtDesc(UUID archiverId);

    void deleteByIdAndArchiverId(Long id, UUID archiverId);
}
