package zero.conflict.archiview.user.application.archiver.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.FollowRepository;
import zero.conflict.archiview.user.application.port.out.PostClient;
import zero.conflict.archiview.user.application.port.out.SearchHistoryRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Hashtags;
import zero.conflict.archiview.user.domain.SearchHistory;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.dto.SearchDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiverSearchQueryService {

    private static final int ALL_TAB_LIMIT = 3;
    private static final int RECENT_MAX = 7;
    private static final String INSTAGRAM_PREFIX = "https://www.instagram.com/";
    private static final String URL_DISPLAY = "https://www.insta..";

    private final UserRepository userRepository;
    private final EditorProfileRepository editorProfileRepository;
    private final FollowRepository followRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final PostClient postClient;

    @Transactional
    public SearchDto.Response search(UUID archiverId, String query, SearchDto.Tab tab) {
        validateArchiverOrEditor(archiverId);
        String normalized = normalize(query);
        if (normalized.isBlank()) {
            return SearchDto.Response.builder()
                    .query(query)
                    .tab(tab)
                    .placeCount(0)
                    .editorCount(0)
                    .hasMorePlaces(false)
                    .hasMoreEditors(false)
                    .places(List.of())
                    .editors(List.of())
                    .build();
        }

        SearchDto.KeywordType keywordType = detectType(normalized);
        saveSearchHistory(archiverId, query, normalized, keywordType);

        List<PostClient.PostPlaceView> visiblePostPlaces = postClient.findAllVisibleByArchiverId(
                archiverId);

        Map<Long, List<PostClient.PostPlaceView>> visibleByPlaceId = visiblePostPlaces.stream()
                .filter(pp -> pp.placeId() != null)
                .collect(Collectors.groupingBy(PostClient.PostPlaceView::placeId));
        Map<UUID, List<PostClient.PostPlaceView>> visibleByEditorId = visiblePostPlaces.stream()
                .filter(pp -> pp.editorId() != null)
                .collect(Collectors.groupingBy(PostClient.PostPlaceView::editorId));

        Set<Long> matchedPlaceIds = matchPlaceIds(visiblePostPlaces, normalized, keywordType);
        List<SearchDto.PlaceCard> allPlaceCards = matchedPlaceIds.stream()
                .map(placeId -> toPlaceCard(visibleByPlaceId.get(placeId)))
                .filter(card -> card != null)
                .sorted(Comparator
                        .comparing(SearchDto.PlaceCard::getLatestUpdatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();

        Set<UUID> matchedEditorIds = matchEditorIds(
                normalized,
                keywordType,
                visiblePostPlaces,
                visibleByEditorId,
                matchedPlaceIds);
        Map<UUID, EditorProfile> profileMap = editorProfileRepository
                .findAllByUserIds(new ArrayList<>(matchedEditorIds))
                .stream()
                .collect(Collectors.toMap(EditorProfile::getUserId, p -> p));

        Set<UUID> followingEditorIds = followRepository.findAllByArchiverId(archiverId).stream()
                .map(follow -> follow.getEditorId())
                .collect(Collectors.toSet());

        List<SearchDto.EditorCard> allEditorCards = matchedEditorIds.stream()
                .map(editorId -> toEditorCard(
                        editorId,
                        profileMap.get(editorId),
                        visibleByEditorId.get(editorId),
                        followingEditorIds))
                .filter(card -> card != null)
                .sorted(Comparator
                        .comparing(SearchDto.EditorCard::getLatestUpdatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();

        return toTabResponse(query, tab, allPlaceCards, allEditorCards);
    }

    @Transactional(readOnly = true)
    public SearchDto.RecentListResponse getRecentSearches(UUID archiverId) {
        validateArchiverOrEditor(archiverId);
        List<SearchDto.RecentItem> histories = searchHistoryRepository
                .findAllByArchiverIdOrderByLastModifiedAtDesc(archiverId)
                .stream()
                .limit(RECENT_MAX)
                .map(this::toRecentItem)
                .toList();
        return SearchDto.RecentListResponse.from(histories);
    }

    @Transactional
    public void deleteRecentSearch(UUID archiverId, Long historyId) {
        validateArchiverOrEditor(archiverId);
        searchHistoryRepository.deleteByIdAndArchiverId(historyId, archiverId);
    }

    @Transactional(readOnly = true)
    public SearchDto.RecommendationListResponse getRecommendations(UUID archiverId) {
        validateArchiverOrEditor(archiverId);
        List<EditorProfile> profiles = editorProfileRepository.findAll();
        if (profiles.isEmpty()) {
            return SearchDto.RecommendationListResponse.from(List.of());
        }

        Map<String, Long> counts = new HashMap<>();
        Map<String, LocalDateTime> latest = new HashMap<>();
        for (EditorProfile profile : profiles) {
            Hashtags hashtags = profile.getHashtags();
            if (hashtags == null) {
                continue;
            }
            LocalDateTime updatedAt = lastUpdatedAt(profile);
            addKeyword(counts, latest, hashtags.getPrimaryTag(), updatedAt);
            addKeyword(counts, latest, hashtags.getSecondaryTag(), updatedAt);
        }

        List<SearchDto.RecommendationItem> items = counts.entrySet().stream()
                .map(entry -> SearchDto.RecommendationItem.builder()
                        .keyword(entry.getKey())
                        .count(entry.getValue())
                        .latestUsedAt(latest.get(entry.getKey()))
                        .build())
                .sorted(Comparator
                        .comparing(SearchDto.RecommendationItem::getCount, Comparator.reverseOrder())
                        .thenComparing(SearchDto.RecommendationItem::getLatestUsedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SearchDto.RecommendationItem::getKeyword,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(RECENT_MAX)
                .toList();

        return SearchDto.RecommendationListResponse.from(items);
    }

    private void addKeyword(
            Map<String, Long> counts,
            Map<String, LocalDateTime> latest,
            String keyword,
            LocalDateTime updatedAt) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        String trimmed = keyword.trim();
        counts.merge(trimmed, 1L, Long::sum);
        latest.merge(trimmed, updatedAt, (oldValue, newValue) -> {
            if (oldValue == null) {
                return newValue;
            }
            if (newValue == null) {
                return oldValue;
            }
            return newValue.isAfter(oldValue) ? newValue : oldValue;
        });
    }

    private SearchDto.Response toTabResponse(
            String query,
            SearchDto.Tab tab,
            List<SearchDto.PlaceCard> allPlaceCards,
            List<SearchDto.EditorCard> allEditorCards) {
        List<SearchDto.PlaceCard> places = allPlaceCards;
        List<SearchDto.EditorCard> editors = allEditorCards;
        boolean hasMorePlaces = false;
        boolean hasMoreEditors = false;

        if (tab == SearchDto.Tab.ALL) {
            hasMorePlaces = allPlaceCards.size() > ALL_TAB_LIMIT;
            hasMoreEditors = allEditorCards.size() > ALL_TAB_LIMIT;
            places = allPlaceCards.stream().limit(ALL_TAB_LIMIT).toList();
            editors = allEditorCards.stream().limit(ALL_TAB_LIMIT).toList();
        } else if (tab == SearchDto.Tab.PLACE) {
            editors = List.of();
        } else if (tab == SearchDto.Tab.EDITOR) {
            places = List.of();
        }

        return SearchDto.Response.builder()
                .query(query)
                .tab(tab)
                .placeCount(allPlaceCards.size())
                .editorCount(allEditorCards.size())
                .hasMorePlaces(hasMorePlaces)
                .hasMoreEditors(hasMoreEditors)
                .places(places)
                .editors(editors)
                .build();
    }

    private SearchDto.PlaceCard toPlaceCard(List<PostClient.PostPlaceView> postPlaces) {
        if (postPlaces == null || postPlaces.isEmpty()) {
            return null;
        }
        PostClient.PostPlaceView latestPostPlace = postPlaces.stream()
                .max(Comparator.comparing(this::lastUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        if (latestPostPlace == null) {
            return null;
        }
        long saveCount = postPlaces.stream().map(PostClient.PostPlaceView::saveCount).mapToLong(this::defaultZero)
                .sum();
        long viewCount = postPlaces.stream().map(PostClient.PostPlaceView::viewCount).mapToLong(this::defaultZero)
                .sum();

        return SearchDto.PlaceCard.builder()
                .placeId(latestPostPlace.placeId())
                .placeName(latestPostPlace.placeName())
                .imageUrl(latestPostPlace.imageUrl())
                .summary(latestPostPlace.description())
                .addressName(latestPostPlace.addressName())
                .roadAddressName(latestPostPlace.roadAddressName())
                .saveCount(saveCount)
                .viewCount(viewCount)
                .latestUpdatedAt(lastUpdatedAt(latestPostPlace))
                .build();
    }

    private SearchDto.EditorCard toEditorCard(
            UUID editorId,
            EditorProfile profile,
            List<PostClient.PostPlaceView> postPlaces,
            Set<UUID> followingEditorIds) {
        if (profile == null) {
            return null;
        }
        Hashtags hashtags = profile.getHashtags();
        LocalDateTime latestPostUpdatedAt = postPlaces == null ? null
                : postPlaces.stream()
                        .map(this::lastUpdatedAt)
                        .max(Comparator.nullsLast(Comparator.naturalOrder()))
                        .orElse(null);
        LocalDateTime profileUpdatedAt = lastUpdatedAt(profile);
        LocalDateTime latestUpdatedAt = latestPostUpdatedAt;
        if (latestUpdatedAt == null || (profileUpdatedAt != null && profileUpdatedAt.isAfter(latestUpdatedAt))) {
            latestUpdatedAt = profileUpdatedAt;
        }

        return SearchDto.EditorCard.builder()
                .editorId(editorId)
                .nickname(profile.getNickname())
                .instagramId(profile.getInstagramId())
                .introduction(profile.getIntroduction())
                .profileImageUrl(profile.getProfileImageUrl())
                .hashtags(hashtags != null ? List.of(hashtags.getPrimaryTag(), hashtags.getSecondaryTag()) : List.of())
                .following(followingEditorIds.contains(editorId))
                .latestUpdatedAt(latestUpdatedAt)
                .build();
    }

    private Set<Long> matchPlaceIds(
            List<PostClient.PostPlaceView> visiblePostPlaces,
            String normalized,
            SearchDto.KeywordType keywordType) {
        return visiblePostPlaces.stream()
                .filter(pp -> matchesPostPlace(pp, normalized, keywordType))
                .map(PostClient.PostPlaceView::placeId)
                .filter(placeId -> placeId != null)
                .collect(Collectors.toSet());
    }

    private Set<UUID> matchEditorIds(
            String normalized,
            SearchDto.KeywordType keywordType,
            List<PostClient.PostPlaceView> visiblePostPlaces,
            Map<UUID, List<PostClient.PostPlaceView>> visibleByEditorId,
            Set<Long> matchedPlaceIds) {
        Set<UUID> matchedByPostPlace = visiblePostPlaces.stream()
                .filter(pp -> matchesPostPlace(pp, normalized, keywordType))
                .map(PostClient.PostPlaceView::editorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<UUID> matchedByProfile = editorProfileRepository.findAll().stream()
                .filter(profile -> matchesEditorProfile(profile, normalized))
                .map(EditorProfile::getUserId)
                .collect(Collectors.toSet());

        Set<UUID> matchedByPlace = visiblePostPlaces.stream()
                .filter(pp -> pp.placeId() != null && matchedPlaceIds.contains(pp.placeId()))
                .map(PostClient.PostPlaceView::editorId)
                .collect(Collectors.toSet());

        Set<UUID> result = new HashSet<>();
        result.addAll(matchedByPostPlace);
        result.addAll(matchedByProfile);
        result.addAll(matchedByPlace);

        // 에디터 탭/전체 탭에서 보여줄 수 있는 유효 에디터만 유지
        result = result.stream()
                .filter(editorId -> visibleByEditorId.containsKey(editorId) || hasProfile(editorId))
                .collect(Collectors.toSet());
        return result;
    }

    private boolean hasProfile(UUID editorId) {
        return editorProfileRepository.findByUserId(editorId).isPresent();
    }

    private boolean matchesPostPlace(PostClient.PostPlaceView pp, String normalized,
            SearchDto.KeywordType keywordType) {
        if (keywordType == SearchDto.KeywordType.URL) {
            return contains(pp.postUrl(), normalized);
        }

        boolean placeMatch = contains(pp.placeName(), normalized)
                || contains(pp.addressName(), normalized)
                || contains(pp.roadAddressName(), normalized);
        boolean postHashTagMatch = pp.hashTags() != null
                && pp.hashTags().stream().anyMatch(tag -> contains(tag, normalized));
        boolean descriptionMatch = contains(pp.description(), normalized);
        boolean urlMatch = contains(pp.postUrl(), normalized);
        return placeMatch || postHashTagMatch || descriptionMatch || urlMatch;
    }

    private boolean matchesEditorProfile(EditorProfile profile, String normalized) {
        Hashtags hashtags = profile.getHashtags();
        return contains(profile.getNickname(), normalized)
                || contains(profile.getInstagramId(), normalized)
                || contains(profile.getIntroduction(), normalized)
                || contains(hashtags != null ? hashtags.getPrimaryTag() : null, normalized)
                || contains(hashtags != null ? hashtags.getSecondaryTag() : null, normalized);
    }

    private boolean contains(String source, String target) {
        if (source == null || target == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT));
    }

    private SearchDto.KeywordType detectType(String normalized) {
        if (normalized.startsWith(INSTAGRAM_PREFIX)) {
            return SearchDto.KeywordType.URL;
        }
        return SearchDto.KeywordType.KEYWORD;
    }

    private String normalize(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private SearchDto.RecentItem toRecentItem(SearchHistory history) {
        String displayKeyword = history.getKeywordType() == SearchDto.KeywordType.URL ? URL_DISPLAY
                : history.getKeyword();
        return SearchDto.RecentItem.builder()
                .historyId(history.getId())
                .keyword(history.getKeyword())
                .displayKeyword(displayKeyword)
                .keywordType(history.getKeywordType())
                .searchedAt(lastUpdatedAt(history))
                .build();
    }

    private void saveSearchHistory(
            UUID archiverId,
            String rawQuery,
            String normalizedQuery,
            SearchDto.KeywordType keywordType) {
        SearchHistory history = searchHistoryRepository
                .findByArchiverIdAndKeywordNormalized(archiverId, normalizedQuery)
                .orElse(null);
        if (history == null) {
            history = SearchHistory.createOf(
                    archiverId,
                    rawQuery.trim(),
                    normalizedQuery,
                    keywordType);
        } else {
            history.updateKeyword(rawQuery.trim(), keywordType);
        }
        searchHistoryRepository.save(history);

        List<SearchHistory> all = searchHistoryRepository.findAllByArchiverIdOrderByLastModifiedAtDesc(archiverId);
        if (all.size() <= RECENT_MAX) {
            return;
        }
        for (int i = RECENT_MAX; i < all.size(); i++) {
            searchHistoryRepository.deleteByIdAndArchiverId(all.get(i).getId(), archiverId);
        }
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    private LocalDateTime lastUpdatedAt(PostClient.PostPlaceView postPlace) {
        return postPlace.lastModifiedAt() != null ? postPlace.lastModifiedAt() : postPlace.createdAt();
    }

    private LocalDateTime lastUpdatedAt(EditorProfile profile) {
        return profile.getLastModifiedAt() != null ? profile.getLastModifiedAt() : profile.getCreatedAt();
    }

    private LocalDateTime lastUpdatedAt(SearchHistory history) {
        return history.getLastModifiedAt() != null ? history.getLastModifiedAt() : history.getCreatedAt();
    }

    private void validateArchiverOrEditor(UUID archiverId) {
        User user = userRepository.findById(archiverId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        if (user.getRole() != User.Role.ARCHIVER && user.getRole() != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_SEARCHER_ROLE);
        }
    }
}
