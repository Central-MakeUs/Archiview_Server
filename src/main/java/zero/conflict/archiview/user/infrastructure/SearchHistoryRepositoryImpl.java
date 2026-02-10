package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.user.application.port.SearchHistoryRepository;
import zero.conflict.archiview.user.domain.SearchHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SearchHistoryRepositoryImpl implements SearchHistoryRepository {

    private final SearchHistoryJpaRepository searchHistoryJpaRepository;

    @Override
    public SearchHistory save(SearchHistory searchHistory) {
        return searchHistoryJpaRepository.save(searchHistory);
    }

    @Override
    public Optional<SearchHistory> findByArchiverIdAndKeywordNormalized(UUID archiverId, String keywordNormalized) {
        return searchHistoryJpaRepository.findByArchiverIdAndKeywordNormalized(archiverId, keywordNormalized);
    }

    @Override
    public List<SearchHistory> findAllByArchiverIdOrderByLastModifiedAtDesc(UUID archiverId) {
        return searchHistoryJpaRepository.findAllByArchiverIdOrderByLastModifiedAtDesc(archiverId);
    }

    @Override
    public void deleteByIdAndArchiverId(Long id, UUID archiverId) {
        searchHistoryJpaRepository.deleteByIdAndArchiverId(id, archiverId);
    }
}
