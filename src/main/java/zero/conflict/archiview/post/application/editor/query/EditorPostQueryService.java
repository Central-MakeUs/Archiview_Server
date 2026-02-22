package zero.conflict.archiview.post.application.editor.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceArchiveRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceCategory;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorPostQueryService {

    private final PostPlaceRepository postPlaceRepository;
    private final PostPlaceArchiveRepository postPlaceArchiveRepository;
    private final PlaceRepository placeRepository;
    private final UserClient userClient;

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
                        .sorted(Comparator.comparing(PostPlace::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList());
    }

    public EditorUploadedPlaceDto.ListResponse getUploadedPlaces(
            UUID editorId,
            MapFilter filter,
            EditorUploadedPlaceDto.PlaceSort sort,
            List<Long> categoryIds,
            Double latitude,
            Double longitude) {
        validateNearbyCoordinates(filter, latitude, longitude);

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
                .filter(entry -> matchCategories(entry.getValue(), categoryIds))
                .filter(entry -> matchNearby(
                        resolvePlace(entry.getKey(), entry.getValue(), placeMap),
                        filter,
                        latitude,
                        longitude))
                .sorted(getUploadedPlaceComparator(sort))
                .map(entry -> toPlaceCardResponse(entry.getKey(), entry.getValue(), placeMap))
                .toList();

        return EditorUploadedPlaceDto.ListResponse.from(places);
    }

    public EditorMapDto.Response getMapPins(
            UUID editorId,
            MapFilter filter,
            List<Long> categoryIds,
            Double latitude,
            Double longitude) {
        validateNearbyCoordinates(filter, latitude, longitude);

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
                .filter(entry -> matchNearby(placeMap.get(entry.getKey()), filter, latitude, longitude))
                .map(entry -> toPlacePin(entry.getKey(), entry.getValue(), placeMap))
                .filter(pin -> pin != null)
                .toList();

        return EditorMapDto.Response.from(pins);
    }

    public EditorInsightDto.SummaryResponse getInsightSummary(UUID editorId, EditorInsightDto.Period period) {
        Map<UUID, UserClient.EditorSummary> editorProfiles = userClient.getEditorSummaries(List.of(editorId));
        UserClient.EditorSummary editorProfile = editorProfiles.get(editorId);
        if (editorProfile == null) {
            throw new DomainException(PostErrorCode.POST_EDITOR_PROFILE_NOT_FOUND);
        }

        List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);

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
                editorProfile.nickname(),
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
        List<PostPlace> ownPostPlaces = postPlaceRepository.findAllByEditorIdAndPlaceId(editorId, placeId);
        if (ownPostPlaces.isEmpty()) {
            return EditorInsightDto.PlaceDetailResponse.empty(placeId);
        }

        Map<UUID, UserClient.EditorSummary> editorProfiles = userClient.getEditorSummaries(List.of(editorId));
        UserClient.EditorSummary editorProfile = editorProfiles.get(editorId);
        if (editorProfile == null) {
            throw new DomainException(PostErrorCode.POST_EDITOR_PROFILE_NOT_FOUND);
        }

        List<EditorInsightDto.PostPlaceDetailResponse> details = ownPostPlaces.stream()
                .map(postPlace -> EditorInsightDto.PostPlaceDetailResponse.from(
                        editorProfile.nickname(),
                        editorProfile.instagramId(),
                        postPlace))
                .toList();

        Place place = placeRepository.findById(placeId)
                .orElseGet(() -> ownPostPlaces.stream()
                        .map(PostPlace::getPlace)
                        .filter(candidate -> candidate != null && placeId.equals(candidate.getId()))
                        .findFirst()
                        .orElse(null));
        if (place == null) {
            throw new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND);
        }

        List<PostPlace> allPlacePostPlaces = postPlaceRepository.findAllByPlaceId(placeId);
        if (allPlacePostPlaces == null || allPlacePostPlaces.isEmpty()) {
            allPlacePostPlaces = ownPostPlaces;
        }

        EditorUploadedPlaceDto.Stats summedStats = sumStats(ownPostPlaces);
        EditorInsightDto.Stats detailStats = EditorInsightDto.Stats.from(
                summedStats.getSaveCount(),
                summedStats.getViewCount(),
                summedStats.getInstagramInflowCount(),
                summedStats.getDirectionCount());

        Long editorTotal = (long) allPlacePostPlaces.stream()
                .map(PostPlace::getEditorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();

        List<Long> placePostPlaceIds = allPlacePostPlaces.stream()
                .map(PostPlace::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        long archiverSaveTotal = postPlaceArchiveRepository.countByPostPlaceIdIn(placePostPlaceIds);

        String latestImageUrl = ownPostPlaces.stream()
                .max(Comparator.comparing(this::getLastUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(PostPlace::getImageUrl)
                .orElse(null);

        return EditorInsightDto.PlaceDetailResponse.from(
                place,
                latestImageUrl,
                editorTotal,
                archiverSaveTotal,
                detailStats,
                details);
    }

    private EditorUploadedPlaceDto.PlaceCardResponse toPlaceCardResponse(
            Long placeId,
            List<PostPlace> postPlaces,
            Map<Long, Place> placeMap) {
        PostPlace latestPostPlace = postPlaces.stream()
                .max(Comparator.comparing(this::getLastUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow();

        Place place = placeMap.get(placeId);
        return EditorUploadedPlaceDto.PlaceCardResponse.from(
                place,
                latestPostPlace,
                sumStats(postPlaces));
    }

    private EditorMapDto.PlacePinResponse toPlacePin(
            Long placeId,
            List<PostPlace> postPlaces,
            Map<Long, Place> placeMap) {
        Place place = placeMap.get(placeId);
        if (!hasValidCoordinates(place)) {
            return null;
        }

        return EditorMapDto.PlacePinResponse.from(place, postPlaces);
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

    private Place resolvePlace(Long placeId, List<PostPlace> postPlaces, Map<Long, Place> placeMap) {
        Place place = placeMap.get(placeId);
        if (place != null) {
            return place;
        }
        return postPlaces.stream()
                .map(PostPlace::getPlace)
                .filter(candidate -> candidate != null && placeId.equals(candidate.getId()))
                .findFirst()
                .orElse(null);
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
        if (!hasValidCoordinates(place)) {
            return false;
        }
        return isWithin1km(place.getPosition(), latitude, longitude);
    }

    private boolean hasValidCoordinates(Place place) {
        return place != null
                && place.getPosition() != null
                && place.getPosition().getLatitude() != null
                && place.getPosition().getLongitude() != null;
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
                .max(Comparator.comparing(this::getLastUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
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
                    (EditorInsightDto.PlaceCardResponse p) -> p.getStats().getInstagramInflowCount(),
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed();
            case MOST_DIRECTIONS -> Comparator.comparing(
                    (EditorInsightDto.PlaceCardResponse p) -> p.getStats().getDirectionCount(),
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
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

    private LocalDateTime getLatestCreatedAt(List<PostPlace> postPlaces) {
        return postPlaces.stream()
                .map(PostPlace::getCreatedAt)
                .max(Comparator.nullsLast(Comparator.naturalOrder()))
                .orElse(null);
    }

    private Comparator<Map.Entry<Long, List<PostPlace>>> getUploadedPlaceComparator(EditorUploadedPlaceDto.PlaceSort sort) {
        Comparator<Map.Entry<Long, List<PostPlace>>> baseComparator = switch (sort) {
            case UPDATED -> Comparator.comparing(
                    (Map.Entry<Long, List<PostPlace>> entry) -> getLatestUpdatedAt(entry.getValue()),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case CREATED -> Comparator.comparing(
                    (Map.Entry<Long, List<PostPlace>> entry) -> getLatestCreatedAt(entry.getValue()),
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };

        return baseComparator
                .reversed()
                .thenComparing(Map.Entry<Long, List<PostPlace>>::getKey, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
