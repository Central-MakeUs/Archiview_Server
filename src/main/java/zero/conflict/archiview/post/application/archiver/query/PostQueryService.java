package zero.conflict.archiview.post.application.archiver.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceArchiveRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;
import zero.conflict.archiview.post.domain.PostPlaceCategory;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceArchive;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverArchivedPostPlaceDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;
import zero.conflict.archiview.post.infrastructure.persistence.CategoryPlaceReadRepository;

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
        private final PostPlaceArchiveRepository postPlaceArchiveRepository;
        private final PlaceRepository placeRepository;
        private final UserClient userClient;
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
                if (!userClient.existsEditorProfile(userId)) {
                        throw new DomainException(PostErrorCode.POST_EDITOR_PROFILE_NOT_FOUND);
                }

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

        public ArchiverHotPlaceDto.ListResponse getHotPlaces(int limit) {
                return getHotPlaces(limit, null);
        }

        public ArchiverArchivedPostPlaceDto.ListResponse getMyArchivedPostPlaces(UUID archiverId) {
                List<PostPlaceArchive> archives = postPlaceArchiveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId);
                if (archives.isEmpty()) {
                        return ArchiverArchivedPostPlaceDto.ListResponse.empty();
                }

                List<Long> postPlaceIds = archives.stream()
                                .map(PostPlaceArchive::getPostPlaceId)
                                .distinct()
                                .toList();
                Map<Long, PostPlace> postPlaceById = postPlaceRepository.findAllByIds(postPlaceIds).stream()
                                .collect(Collectors.toMap(PostPlace::getId, Function.identity(), (a, b) -> a));

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = archiverVisibilityService
                                .getVisibilityFilter(archiverId);
                List<ArchiverArchivedPostPlaceDto.ArchivedPostPlaceResponse> responses = archives.stream()
                                .map(archive -> toArchivedPostPlaceResponse(
                                                archive,
                                                postPlaceById.get(archive.getPostPlaceId()),
                                                visibilityFilter))
                                .filter(response -> response != null)
                                .toList();

                return ArchiverArchivedPostPlaceDto.ListResponse.from(responses);
        }

        public EditorMapDto.Response getMyArchivedMapPins(
                        MapFilter filter,
                        List<Long> categoryIds,
                        Double latitude,
                        Double longitude,
                        UUID archiverId) {
                validateNearbyCoordinates(filter, latitude, longitude);

                List<PostPlaceArchive> archives = postPlaceArchiveRepository.findAllByArchiverIdOrderByCreatedAtDesc(archiverId);
                if (archives.isEmpty()) {
                        return EditorMapDto.Response.empty();
                }

                List<Long> postPlaceIds = archives.stream()
                                .map(PostPlaceArchive::getPostPlaceId)
                                .distinct()
                                .toList();
                List<PostPlace> postPlaces = postPlaceRepository.findAllByIds(postPlaceIds);
                if (postPlaces.isEmpty()) {
                        return EditorMapDto.Response.empty();
                }

                ArchiverVisibilityService.VisibilityFilter visibilityFilter = archiverVisibilityService
                                .getVisibilityFilter(archiverId);
                List<PostPlace> visiblePostPlaces = archiverVisibilityService.filterVisiblePostPlaces(
                                postPlaces,
                                visibilityFilter);
                if (visiblePostPlaces.isEmpty()) {
                        return EditorMapDto.Response.empty();
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = visiblePostPlaces.stream()
                                .filter(postPlace -> postPlace.getPlace() != null && postPlace.getPlace().getId() != null)
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));
                if (postPlacesByPlaceId.isEmpty()) {
                        return EditorMapDto.Response.empty();
                }

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

                Map<UUID, UserClient.EditorSummary> editorProfileMap = userClient.getEditorSummaries(editorIds);
                Set<Long> archivedPostPlaceIds = getArchivedPostPlaceIds(postPlaces, archiverId);

                List<ArchiverPlaceDetailDto.PostPlaceResponse> postPlaceResponses = postPlaces.stream()
                                .map(pp -> ArchiverPlaceDetailDto.PostPlaceResponse.from(
                                                pp,
                                                editorProfileMap.get(pp.getEditorId()) != null
                                                                ? editorProfileMap.get(pp.getEditorId()).nickname()
                                                                : null,
                                                editorProfileMap.get(pp.getEditorId()) != null
                                                                ? editorProfileMap.get(pp.getEditorId()).instagramId()
                                                                : null,
                                                archivedPostPlaceIds.contains(pp.getId())))
                                .toList();

                return ArchiverPlaceDetailDto.Response.from(placeResponse, postPlaceResponses);
        }

        private Set<Long> getArchivedPostPlaceIds(List<PostPlace> postPlaces, UUID archiverId) {
                if (archiverId == null || postPlaces.isEmpty()) {
                        return Set.of();
                }

                List<Long> postPlaceIds = postPlaces.stream()
                                .map(PostPlace::getId)
                                .filter(id -> id != null)
                                .toList();
                if (postPlaceIds.isEmpty()) {
                        return Set.of();
                }

                return postPlaceArchiveRepository.findAllByArchiverIdAndPostPlaceIdIn(archiverId, postPlaceIds).stream()
                                .map(PostPlaceArchive::getPostPlaceId)
                                .collect(Collectors.toSet());
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
                if (!userClient.existsEditorProfile(editorId)) {
                        throw new DomainException(PostErrorCode.POST_EDITOR_PROFILE_NOT_FOUND);
                }

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

        private LocalDateTime getLastUpdatedAt(PostPlace postPlace) {
                return postPlace.getLastModifiedAt() != null
                                ? postPlace.getLastModifiedAt()
                                : postPlace.getCreatedAt();
        }

        private ArchiverArchivedPostPlaceDto.ArchivedPostPlaceResponse toArchivedPostPlaceResponse(
                        PostPlaceArchive archive,
                        PostPlace postPlace,
                        ArchiverVisibilityService.VisibilityFilter visibilityFilter) {
                if (archive == null || postPlace == null) {
                        return null;
                }
                if (!archiverVisibilityService.isVisible(postPlace, visibilityFilter)) {
                        return null;
                }

                return ArchiverArchivedPostPlaceDto.ArchivedPostPlaceResponse.builder()
                                .postPlaceId(postPlace.getId())
                                .placeId(postPlace.getPlace() != null ? postPlace.getPlace().getId() : null)
                                .placeName(postPlace.getPlace() != null ? postPlace.getPlace().getName() : null)
                                .description(postPlace.getDescription())
                                .imageUrl(postPlace.getImageUrl())
                                .saveCount(defaultZero(postPlace.getSaveCount()))
                                .viewCount(defaultZero(postPlace.getViewCount()))
                                .lastModifiedAt(getLastUpdatedAt(postPlace))
                                .archivedAt(archive.getCreatedAt())
                                .build();
        }

}
