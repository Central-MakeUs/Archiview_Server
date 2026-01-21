package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.PostPlaceCategory;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.presentation.query.dto.EditorMapDto;
import zero.conflict.archiview.post.presentation.query.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.presentation.query.dto.EditorMapDto.MapFilter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostQueryService {

        private static final double MIN_BBOX_SPAN_DEGREES = 0.001;
        private static final double MAX_BBOX_SPAN_DEGREES = 2.0;

        private final PostPlaceRepository postPlaceRepository;
        private final PlaceRepository placeRepository;

        public EditorUploadedPlaceDto.ListResponse getUploadedPlaces(Long editorId) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorUploadedPlaceDto.ListResponse.empty();
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(PostPlace::getPlaceId));

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

                return EditorUploadedPlaceDto.ListResponse.builder()
                                .places(places)
                                .build();
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
                return EditorUploadedPlaceDto.PlaceCardResponse.builder()
                                .placeId(placeId)
                                .placeName(place != null ? place.getName() : null)
                                .placeImageUrl(latestPostPlace.getImageUrl())
                                .editorSummary(latestPostPlace.getDescription())
                                .stats(sumStats(postPlaces))
                                .build();
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

                return EditorUploadedPlaceDto.Stats.builder()
                                .viewCount(viewCount)
                                .saveCount(saveCount)
                                .instagramInflowCount(instagramInflowCount)
                                .directionCount(directionCount)
                                .build();
        }

        private long defaultZero(Long value) {
                return value == null ? 0L : value;
        }

        public EditorMapDto.Response getMapPins(
                        Long editorId,
                        MapFilter filter,
                        Double minLat,
                        Double minLon,
                        Double maxLat,
                        Double maxLon,
                        List<Long> categoryIds) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorMapDto.Response.builder()
                                        .pins(List.of())
                                        .build();
                }
                if (filter == MapFilter.NEARBY) {
                        validateBbox(minLat, minLon, maxLat, maxLon);
                }

                Map<Long, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(PostPlace::getPlaceId));

                Map<Long, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorMapDto.PlacePinResponse> pins = postPlacesByPlaceId.entrySet().stream()
                                .filter(entry -> matchCategories(entry.getValue(), categoryIds))
                                .map(entry -> toPlacePin(entry.getKey(), entry.getValue(), placeMap))
                                .filter(pin -> pin != null)
                                .filter(pin -> filterPin(pin, filter, minLat, minLon, maxLat, maxLon))
                                .toList();

                return EditorMapDto.Response.builder()
                                .pins(pins)
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

                List<String> categoryNames = postPlaces.stream()
                                .flatMap(postPlace -> postPlace.getPostPlaceCategories().stream())
                                .map(PostPlaceCategory::getCategory)
                                .filter(category -> category != null && category.getName() != null)
                                .map(category -> category.getName())
                                .distinct()
                                .toList();

                return EditorMapDto.PlacePinResponse.builder()
                                .placeId(placeId)
                                .name(place.getName())
                                .latitude(place.getPosition().getLatitude())
                                .longitude(place.getPosition().getLongitude())
                                .categories(categoryNames)
                                .build();
        }

        private boolean filterPin(
                        EditorMapDto.PlacePinResponse pin,
                        MapFilter filter,
                        Double minLat,
                        Double minLon,
                        Double maxLat,
                        Double maxLon) {
                if (filter == null || filter == MapFilter.ALL) {
                        return true;
                }
                if (filter == MapFilter.NEARBY) {
                        return isInsideBbox(pin, minLat, minLon, maxLat, maxLon);
                }
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

        private boolean isInsideBbox(
                        EditorMapDto.PlacePinResponse pin,
                        double minLat,
                        double minLon,
                        double maxLat,
                        double maxLon) {
                return pin.getLatitude() >= minLat
                                && pin.getLatitude() <= maxLat
                                && pin.getLongitude() >= minLon
                                && pin.getLongitude() <= maxLon;
        }

        private void validateBbox(Double minLat, Double minLon, Double maxLat, Double maxLon) {
                if (minLat == null || minLon == null || maxLat == null || maxLon == null) {
                        throw new DomainException(PostErrorCode.INVALID_BBOX_RANGE);
                }
                if (minLat < -90 || maxLat > 90 || minLon < -180 || maxLon > 180) {
                        throw new DomainException(PostErrorCode.INVALID_BBOX_RANGE);
                }
                if (minLat >= maxLat || minLon >= maxLon) {
                        throw new DomainException(PostErrorCode.INVALID_BBOX_RANGE);
                }
                double latSpan = maxLat - minLat;
                double lonSpan = maxLon - minLon;
                if (latSpan < MIN_BBOX_SPAN_DEGREES || lonSpan < MIN_BBOX_SPAN_DEGREES) {
                        throw new DomainException(PostErrorCode.INVALID_BBOX_RANGE);
                }
                if (latSpan > MAX_BBOX_SPAN_DEGREES || lonSpan > MAX_BBOX_SPAN_DEGREES) {
                        throw new DomainException(PostErrorCode.INVALID_BBOX_RANGE);
                }
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
