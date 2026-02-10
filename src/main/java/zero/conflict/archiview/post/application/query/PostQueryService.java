package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlaceCategory;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;
import zero.conflict.archiview.post.infrastructure.CategoryPlaceReadRepository;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostQueryService {

        private final PostPlaceRepository postPlaceRepository;
        private final PlaceRepository placeRepository;
        private final EditorProfileRepository editorProfileRepository;
        private final CategoryPlaceReadRepository categoryPlaceReadRepository;
        private final ArchiverVisibilityService archiverVisibilityService;

        public CategoryQueryDto.CategoryPlaceListResponse getNearbyPlacesWithin1km(
                        Double latitude,
                        Double longitude) {
                Position.of(latitude, longitude);
                return CategoryQueryDto.CategoryPlaceListResponse.from(
                                categoryPlaceReadRepository.findPlaceSummariesNearby(latitude, longitude, 1000));
        }

        public CategoryQueryDto.CategoryPlaceListResponse getNearbyPlacesWithin1km(
                        Double latitude,
                        Double longitude,
                        UUID archiverId) {
                Position.of(latitude, longitude);

                List<CategoryPlaceReadRepository.CategoryPlaceSummaryProjection> nearby = categoryPlaceReadRepository
                                .findPlaceSummariesNearby(latitude, longitude, 1000);
                if (nearby.isEmpty()) {
                        return CategoryQueryDto.CategoryPlaceListResponse.builder()
                                        .totalCount(0L)
                                        .places(List.of())
                                        .build();
                }

                List<Long> placeIds = nearby.stream()
                                .map(CategoryPlaceReadRepository.CategoryPlaceSummaryProjection::getPlaceId)
                                .toList();
                Map<Long, String> placeNameById = nearby.stream()
                                .collect(Collectors.toMap(
                                                CategoryPlaceReadRepository.CategoryPlaceSummaryProjection::getPlaceId,
                                                CategoryPlaceReadRepository.CategoryPlaceSummaryProjection::getPlaceName,
                                                (existing, ignored) -> existing));

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = archiverVisibilityService
                                .getVisibilityFilter(archiverId);
                List<PostPlace> visiblePostPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                                postPlaceRepository.findAllByPlaceIds(placeIds),
                                visibilityFilter);
                Map<Long, List<PostPlace>> visibleByPlaceId = visiblePostPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                List<CategoryQueryDto.CategoryPlaceResponse> places = placeIds.stream()
                                .map(placeId -> toCategoryPlaceResponse(placeId, placeNameById.get(placeId),
                                                visibleByPlaceId.get(placeId)))
                                .filter(response -> response != null)
                                .toList();

                return CategoryQueryDto.CategoryPlaceListResponse.builder()
                                .totalCount((long) places.size())
                                .places(places)
                                .build();
        }

        public ArchiverEditorPostPlaceDto.ListResponse getEditorUploadedPostPlaces(
                        UUID userId,
                        ArchiverEditorPostPlaceDto.Sort sort) {
                return getEditorUploadedPostPlaces(userId, sort, null);
        }

        public ArchiverEditorPostPlaceDto.ListResponse getEditorUploadedPostPlaces(
                        UUID userId,
                        ArchiverEditorPostPlaceDto.Sort sort,
                        UUID archiverId) {
                editorProfileRepository.findByUserId(userId)
                                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(userId);
                if (archiverId != null) {
                        postPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                                        postPlaces,
                                        archiverVisibilityService.getVisibilityFilter(archiverId));
                }
                if (postPlaces.isEmpty()) {
                        return ArchiverEditorPostPlaceDto.ListResponse.empty();
                }

                Comparator<PostPlace> comparator = Comparator.comparing(
                                this::getLastUpdatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder()));
                if (sort == ArchiverEditorPostPlaceDto.Sort.LATEST) {
                        comparator = comparator.reversed();
                }

                List<ArchiverEditorPostPlaceDto.PostPlaceResponse> responses = postPlaces.stream()
                                .sorted(comparator)
                                .map(postPlace -> ArchiverEditorPostPlaceDto.PostPlaceResponse.from(
                                                postPlace,
                                                getLastUpdatedAt(postPlace)))
                                .toList();

                return ArchiverEditorPostPlaceDto.ListResponse.from(responses);
        }

        public EditorPostByPostPlaceDto.Response getPostByPostPlaceId(Long postPlaceId) {
                PostPlace targetPostPlace = postPlaceRepository.findById(postPlaceId)
                                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

                if (targetPostPlace.getPost() == null || targetPostPlace.getPost().getId() == null) {
                        throw new DomainException(PostErrorCode.POST_NOT_FOUND);
                }

                Post post = targetPostPlace.getPost();
                List<PostPlace> postPlaces = postPlaceRepository.findAllByPostId(post.getId());
                if (postPlaces.isEmpty()) {
                        postPlaces = List.of(targetPostPlace);
                }

                return EditorPostByPostPlaceDto.Response.from(
                                post,
                                postPlaces.stream()
                                                .sorted(Comparator.comparing(PostPlace::getId,
                                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                                .toList());
        }

        public ArchiverHotPlaceDto.ListResponse getHotPlaces(int limit) {
                return getHotPlaces(limit, null);
        }

        public ArchiverHotPlaceDto.ListResponse getHotPlaces(int limit, UUID archiverId) {
                List<Place> places = placeRepository.findTopByViewCount(limit);
                if (places.isEmpty()) {
                        return ArchiverHotPlaceDto.ListResponse.empty();
                }

                List<Long> placeIds = places.stream()
                                .map(Place::getId)
                                .toList();
                List<PostPlace> postPlaces = postPlaceRepository.findAllByPlaceIds(placeIds);
                if (archiverId != null) {
                        postPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                                        postPlaces,
                                        archiverVisibilityService.getVisibilityFilter(archiverId));
                }

                Map<Long, PostPlace> latestPostPlaceByPlaceId = postPlaces.stream()
                                .sorted(Comparator.comparing(
                                                this::getLastUpdatedAt,
                                                Comparator.nullsLast(Comparator.naturalOrder()))
                                                .reversed())
                                .collect(Collectors.toMap(
                                                pp -> pp.getPlace().getId(),
                                                Function.identity(),
                                                (existing, ignored) -> existing));

                List<ArchiverHotPlaceDto.PlaceCardResponse> cards = places.stream()
                                .filter(place -> latestPostPlaceByPlaceId.containsKey(place.getId()))
                                .map(place -> ArchiverHotPlaceDto.PlaceCardResponse.from(
                                                place,
                                                latestPostPlaceByPlaceId.get(place.getId())))
                                .toList();

                return ArchiverHotPlaceDto.ListResponse.from(cards);
        }

        public EditorUploadedPlaceDto.ListResponse getUploadedPlaces(UUID editorId) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorUploadedPlaceDto.ListResponse.empty();
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<Long, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorUploadedPlaceDto.PlaceCardResponse> places = postPlacesByPlaceId.entrySet().stream()
                                .sorted(Comparator.comparing(
                                                (Map.Entry<Long, List<PostPlace>> entry) -> getLatestUpdatedAt(
                                                                entry.getValue()),
                                                Comparator.nullsLast(Comparator.naturalOrder()))
                                                .reversed())
                                .map(entry -> toPlaceCardResponse(entry.getKey(), entry.getValue(), placeMap))
                                .toList();

                return EditorUploadedPlaceDto.ListResponse.from(places);
        }

        private EditorUploadedPlaceDto.PlaceCardResponse toPlaceCardResponse(
                        Long placeId,
                        List<PostPlace> postPlaces,
                        Map<Long, Place> placeMap) {
                PostPlace latestPostPlace = postPlaces.stream()
                                .max(Comparator.comparing(this::getLastUpdatedAt,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .orElseThrow();

                Place place = placeMap.get(placeId);
                return EditorUploadedPlaceDto.PlaceCardResponse.from(
                                place,
                                latestPostPlace,
                                sumStats(postPlaces));
        }

        private EditorUploadedPlaceDto.Stats sumStats(List<PostPlace> postPlaces) {
                long viewCount = 0;
                long saveCount = 0;
                long instagramInflowCount = 0;
                long directionCount = 0;

                for (PostPlace postPlace : postPlaces) {
                        viewCount += defaultZero(postPlace.getViewCount());
                        saveCount += defaultZero(postPlace.getSaveCount());
                        instagramInflowCount += defaultZero(postPlace.getInstagramInflowCount());
                        directionCount += defaultZero(postPlace.getDirectionCount());
                }

                return EditorUploadedPlaceDto.Stats.from(saveCount, viewCount, instagramInflowCount, directionCount);
        }

        private long defaultZero(Long value) {
                return value == null ? 0L : value;
        }

        public ArchiverPlaceDetailDto.Response getArchiverPlaceDetail(Long placeId) {
                return getArchiverPlaceDetail(placeId, null);
        }

        public ArchiverPlaceDetailDto.Response getArchiverPlaceDetail(Long placeId, UUID archiverId) {
                Place place = placeRepository.findById(placeId)
                                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

                List<PostPlace> postPlaces = postPlaceRepository.findAllByPlaceId(placeId);
                if (archiverId != null) {
                        postPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                                        postPlaces,
                                        archiverVisibilityService.getVisibilityFilter(archiverId));
                }

                EditorUploadedPlaceDto.Stats summedStats = sumStats(postPlaces);
                ArchiverPlaceDetailDto.PlaceResponse placeResponse = ArchiverPlaceDetailDto.PlaceResponse.from(
                                place,
                                summedStats.getSaveCount(),
                                summedStats.getInstagramInflowCount(),
                                summedStats.getDirectionCount());

                if (postPlaces.isEmpty()) {
                        return ArchiverPlaceDetailDto.Response.empty(placeResponse);
                }

                List<UUID> editorIds = postPlaces.stream()
                                .map(PostPlace::getEditorId)
                                .distinct()
                                .toList();

                Map<UUID, EditorProfile> editorProfileMap = editorProfileRepository.findAllByUserIds(editorIds).stream()
                                .collect(Collectors.toMap(EditorProfile::getUserId, Function.identity()));

                List<ArchiverPlaceDetailDto.PostPlaceResponse> postPlaceResponses = postPlaces.stream()
                                .map(pp -> ArchiverPlaceDetailDto.PostPlaceResponse.from(
                                                pp, editorProfileMap.get(pp.getEditorId())))
                                .toList();

                return ArchiverPlaceDetailDto.Response.from(placeResponse, postPlaceResponses);
        }

        public EditorMapDto.Response getMapPins(
                        UUID editorId,
                        MapFilter filter,
                        List<Long> categoryIds) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorMapDto.Response.empty();
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<Long, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorMapDto.PlacePinResponse> pins = postPlacesByPlaceId.entrySet().stream()
                                .filter(entry -> matchCategories(entry.getValue(), categoryIds))
                                .map(entry -> toPlacePin(entry.getKey(), entry.getValue(), placeMap))
                                .filter(pin -> pin != null)
                                .filter(pin -> filterPin(pin, filter))
                                .toList();

                return EditorMapDto.Response.from(pins);
        }

        public EditorMapDto.Response getMapPinsForArchiver(
                        UUID editorId,
                        MapFilter filter,
                        List<Long> categoryIds,
                        Double latitude,
                        Double longitude) {
                return getMapPinsForArchiver(editorId, filter, categoryIds, latitude, longitude, null);
        }

        public EditorMapDto.Response getMapPinsForArchiver(
                        UUID editorId,
                        MapFilter filter,
                        List<Long> categoryIds,
                        Double latitude,
                        Double longitude,
                        UUID archiverId) {
                editorProfileRepository.findByUserId(editorId)
                                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

                validateNearbyCoordinates(filter, latitude, longitude);

                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (archiverId != null) {
                        postPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                                        postPlaces,
                                        archiverVisibilityService.getVisibilityFilter(archiverId));
                }
                if (postPlaces.isEmpty()) {
                        return EditorMapDto.Response.empty();
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<Long, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorMapDto.PlacePinResponse> pins = postPlacesByPlaceId.entrySet().stream()
                                .filter(entry -> matchAllCategories(entry.getValue(), categoryIds))
                                .filter(entry -> matchNearby(placeMap.get(entry.getKey()), filter, latitude, longitude))
                                .map(entry -> toPlacePin(entry.getKey(), entry.getValue(), placeMap))
                                .filter(pin -> pin != null)
                                .toList();

                return EditorMapDto.Response.from(pins);
        }

        private CategoryQueryDto.CategoryPlaceResponse toCategoryPlaceResponse(
                        Long placeId,
                        String placeName,
                        List<PostPlace> postPlaces) {
                if (postPlaces == null || postPlaces.isEmpty()) {
                        return null;
                }

                PostPlace latestPostPlace = postPlaces.stream()
                                .max(Comparator.comparing(this::getLastUpdatedAt,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .orElse(null);
                if (latestPostPlace == null) {
                        return null;
                }

                long viewCount = postPlaces.stream()
                                .map(PostPlace::getViewCount)
                                .mapToLong(this::defaultZero)
                                .sum();
                long saveCount = postPlaces.stream()
                                .map(PostPlace::getSaveCount)
                                .mapToLong(this::defaultZero)
                                .sum();

                return CategoryQueryDto.CategoryPlaceResponse.builder()
                                .placeId(placeId)
                                .placeName(placeName)
                                .latestDescription(latestPostPlace.getDescription())
                                .viewCount(viewCount)
                                .saveCount(saveCount)
                                .build();
        }

        public EditorInsightDto.SummaryResponse getInsightSummary(UUID editorId, EditorInsightDto.Period period) {
                EditorProfile editorProfile = editorProfileRepository.findByUserId(editorId)
                                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);

                // 기간(period) 필터링
                LocalDateTime now = LocalDateTime.now();
                postPlaces = postPlaces.stream()
                                .filter(pp -> matchesPeriod(pp, period, now))
                                .toList();

                long totalPlaceCount = postPlaces.stream()
                                .map(pp -> pp.getPlace().getId())
                                .distinct()
                                .count();

                long viewCount = 0;
                long saveCount = 0;
                long instagramInflowCount = 0;

                for (PostPlace postPlace : postPlaces) {
                        viewCount += defaultZero(postPlace.getViewCount());
                        saveCount += defaultZero(postPlace.getSaveCount());
                        instagramInflowCount += defaultZero(postPlace.getInstagramInflowCount());
                }

                return EditorInsightDto.SummaryResponse.from(
                                editorProfile,
                                totalPlaceCount,
                                instagramInflowCount,
                                saveCount,
                                viewCount,
                                period);
        }

        public EditorInsightDto.PlaceCardListResponse getInsightPlaces(UUID editorId, EditorInsightDto.PlaceSort sort) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorInsightDto.PlaceCardListResponse.empty(sort);
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<Long, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorInsightDto.PlaceCardResponse> places = postPlacesByPlaceId.entrySet().stream()
                                .map(entry -> toInsightPlaceCardResponse(entry.getKey(), entry.getValue(), placeMap))
                                .sorted(getInsightComparator(sort))
                                .toList();

                return EditorInsightDto.PlaceCardListResponse.of(sort, places);
        }

        public EditorInsightDto.PlaceDetailResponse getInsightPlaceDetail(UUID editorId, Long placeId) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorIdAndPlaceId(editorId, placeId);
                if (postPlaces.isEmpty()) {
                        return EditorInsightDto.PlaceDetailResponse.empty(placeId);
                }

                EditorProfile editorProfile = editorProfileRepository.findByUserId(editorId)
                                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

                List<EditorInsightDto.PostPlaceDetailResponse> details = postPlaces.stream()
                                .map(postPlace -> EditorInsightDto.PostPlaceDetailResponse.from(
                                                editorProfile, postPlace))
                                .toList();

                return EditorInsightDto.PlaceDetailResponse.of(placeId, details);
        }

        private EditorMapDto.PlacePinResponse toPlacePin(
                        Long placeId,
                        List<PostPlace> postPlaces,
                        Map<Long, Place> placeMap) {
                Place place = placeMap.get(placeId);
                if (place == null || place.getPosition() == null) {
                        return null;
                }

                return EditorMapDto.PlacePinResponse.from(place, postPlaces);
        }

        private boolean filterPin(
                        EditorMapDto.PlacePinResponse pin,
                        MapFilter filter) {
                if (filter == null || filter == MapFilter.ALL) {
                        return true;
                }
                // NEARBY 필터일 때도 현재는 모든 핀을 반환하도록 설정 (추후 확장 가능)
                return true;
        }

        private boolean matchCategories(List<PostPlace> postPlaces, List<Long> categoryIds) {
                if (categoryIds == null || categoryIds.isEmpty()) {
                        return true;
                }
                return postPlaces.stream()
                                .flatMap(postPlace -> postPlace.getPostPlaceCategories().stream())
                                .map(PostPlaceCategory::getCategory)
                                .filter(category -> category != null)
                                .map(category -> category.getId())
                                .anyMatch(categoryIds::contains);
        }

        private boolean matchAllCategories(List<PostPlace> postPlaces, List<Long> categoryIds) {
                if (categoryIds == null || categoryIds.isEmpty()) {
                        return true;
                }

                Set<Long> ownedCategoryIds = postPlaces.stream()
                                .flatMap(postPlace -> postPlace.getPostPlaceCategories().stream())
                                .map(PostPlaceCategory::getCategory)
                                .filter(category -> category != null && category.getId() != null)
                                .map(category -> category.getId())
                                .collect(Collectors.toSet());

                return ownedCategoryIds.containsAll(new HashSet<>(categoryIds));
        }

        private void validateNearbyCoordinates(MapFilter filter, Double latitude, Double longitude) {
                if (filter != MapFilter.NEARBY) {
                        return;
                }
                Position.of(latitude, longitude);
        }

        private boolean matchNearby(Place place, MapFilter filter, Double latitude, Double longitude) {
                if (filter != MapFilter.NEARBY) {
                        return true;
                }
                if (place == null || place.getPosition() == null) {
                        return false;
                }
                return isWithin1km(place.getPosition(), latitude, longitude);
        }

        private boolean isWithin1km(Position target, Double latitude, Double longitude) {
                final double earthRadiusM = 6371000.0d;
                double lat1 = Math.toRadians(latitude);
                double lon1 = Math.toRadians(longitude);
                double lat2 = Math.toRadians(target.getLatitude());
                double lon2 = Math.toRadians(target.getLongitude());

                double deltaLat = lat2 - lat1;
                double deltaLon = lon2 - lon1;

                double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distanceM = earthRadiusM * c;

                return distanceM <= 1000.0d;
        }

        private boolean matchesPeriod(PostPlace postPlace, EditorInsightDto.Period period, LocalDateTime now) {
                if (period == EditorInsightDto.Period.ALL) {
                        return true;
                }
                LocalDateTime targetDate = (period == EditorInsightDto.Period.MONTH)
                                ? now.minusMonths(1)
                                : now.minusWeeks(1);

                LocalDateTime lastUpdatedAt = getLastUpdatedAt(postPlace);
                return lastUpdatedAt != null && lastUpdatedAt.isAfter(targetDate);
        }

        private EditorInsightDto.PlaceCardResponse toInsightPlaceCardResponse(
                        Long placeId,
                        List<PostPlace> postPlaces,
                        Map<Long, Place> placeMap) {
                PostPlace latestPostPlace = postPlaces.stream()
                                .max(Comparator.comparing(this::getLastUpdatedAt,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .orElseThrow();

                Place place = placeMap.get(placeId);
                EditorUploadedPlaceDto.Stats stats = sumStats(postPlaces);
                EditorInsightDto.Stats insightStats = EditorInsightDto.Stats.from(
                                stats.getSaveCount(),
                                stats.getViewCount(),
                                stats.getInstagramInflowCount(),
                                stats.getDirectionCount());

                return EditorInsightDto.PlaceCardResponse.from(
                                place,
                                latestPostPlace,
                                insightStats,
                                getLastUpdatedAt(latestPostPlace));
        }

        private Comparator<EditorInsightDto.PlaceCardResponse> getInsightComparator(EditorInsightDto.PlaceSort sort) {
                return switch (sort) {
                        case RECENT -> Comparator.comparing(EditorInsightDto.PlaceCardResponse::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                        case MOST_VIEWED -> Comparator.comparing(
                                        (EditorInsightDto.PlaceCardResponse p) -> p.getStats().getViewCount(),
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                        case MOST_SAVED -> Comparator.comparing(
                                        (EditorInsightDto.PlaceCardResponse p) -> p.getStats().getSaveCount(),
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                        case MOST_INSTAGRAM -> Comparator.comparing(
                                        (EditorInsightDto.PlaceCardResponse p) -> p.getStats()
                                                        .getInstagramInflowCount(),
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                        case MOST_DIRECTIONS -> Comparator.comparing(
                                        (EditorInsightDto.PlaceCardResponse p) -> p.getStats().getDirectionCount(),
                                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                };
        }

        private LocalDateTime getLastUpdatedAt(PostPlace postPlace) {
                return postPlace.getLastModifiedAt() != null
                                ? postPlace.getLastModifiedAt()
                                : postPlace.getCreatedAt();
        }

        private LocalDateTime getLatestUpdatedAt(List<PostPlace> postPlaces) {
                return postPlaces.stream()
                                .map(this::getLastUpdatedAt)
                                .max(Comparator.nullsLast(Comparator.naturalOrder()))
                                .orElse(null);
        }

}
