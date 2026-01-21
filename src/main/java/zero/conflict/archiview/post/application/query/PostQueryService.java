package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.PostPlace;
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

        public EditorMapDto.Response getMapPins(Long editorId, MapFilter filter, Double lat, Double lon,
                        List<Long> categoryIds) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);

                if (categoryIds != null && !categoryIds.isEmpty()) {
                        postPlaces = postPlaces.stream()
                                        .filter(pp -> pp.getPostPlaceCategories().stream()
                                                        .anyMatch(ppc -> categoryIds
                                                                        .contains(ppc.getCategory().getId())))
                                        .toList();
                }

                Map<Long, Place> placeMap = placeRepository.findAllByIds(
                                postPlaces.stream().map(PostPlace::getPlaceId).distinct().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorMapDto.PlacePinResponse> pins = postPlaces.stream()
                                .map(pp -> {
                                        Place place = placeMap.get(pp.getPlaceId());
                                        if (place == null)
                                                return null;
                                        return EditorMapDto.PlacePinResponse.builder()
                                                        .placeId(place.getId())
                                                        .name(place.getName())
                                                        .latitude(place.getPosition().getLatitude())
                                                        .longitude(place.getPosition().getLongitude())
                                                        .categories(pp.getPostPlaceCategories().stream()
                                                                        .map(ppc -> ppc.getCategory().getName())
                                                                        .toList())
                                                        .build();
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toMap(
                                                EditorMapDto.PlacePinResponse::getPlaceId,
                                                Function.identity(),
                                                (p1, p2) -> p1 // Keep first if duplicates
                                ))
                                .values().stream()
                                .toList();

                if (filter == MapFilter.NEARBY && lat != null && lon != null) {
                        pins = pins.stream()
                                        .filter(pin -> calculateDistance(lat, lon, pin.getLatitude(),
                                                        pin.getLongitude()) <= 3000) // Within 3km
                                        .toList();
                }

                return EditorMapDto.Response.builder()
                                .pins(pins)
                                .build();
        }

        private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
                double theta = lon1 - lon2;
                double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
                dist = Math.acos(dist);
                dist = rad2deg(dist);
                dist = dist * 60 * 1.1515 * 1609.344; // To meters
                return (dist);
        }

        private double deg2rad(double deg) {
                return (deg * Math.PI / 180.0);
        }

        private double rad2deg(double rad) {
                return (rad * 180 / Math.PI);
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
