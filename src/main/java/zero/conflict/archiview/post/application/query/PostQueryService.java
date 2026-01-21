package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.presentation.query.dto.EditorUploadedPlaceDto;

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
                        (Map.Entry<Long, List<PostPlace>> entry) -> getLatestUpdatedAt(entry.getValue()),
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
                .max(Comparator.comparing(this::getLastUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
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
