package zero.conflict.archiview.post.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlaceCategory;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostQueryService {

        private final PostPlaceRepository postPlaceRepository;
        private final PlaceRepository placeRepository;
        private final EditorProfileRepository editorProfileRepository;

        public ArchiverHotPlaceDto.ListResponse getHotPlaces(int limit) {
                List<Place> places = placeRepository.findTopByViewCount(limit);
                if (places.isEmpty()) {
                        return ArchiverHotPlaceDto.ListResponse.empty();
                }

                List<UUID> placeIds = places.stream()
                                .map(Place::getId)
                                .toList();
                List<PostPlace> postPlaces = postPlaceRepository.findAllByPlaceIds(placeIds);

                Map<UUID, PostPlace> latestPostPlaceByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()))
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> entry.getValue().stream()
                                                                .max(Comparator.comparing(
                                                                                this::getLastUpdatedAt,
                                                                                Comparator.nullsLast(
                                                                                                Comparator.naturalOrder())))
                                                                .orElse(null)));

                List<ArchiverHotPlaceDto.PlaceCardResponse> cards = places.stream()
                                .map(place -> {
                                        PostPlace latestPostPlace = latestPostPlaceByPlaceId.get(place.getId());
                                        return ArchiverHotPlaceDto.PlaceCardResponse.builder()
                                                        .placeId(place.getId())
                                                        .name(place.getName())
                                                        .imageUrl(latestPostPlace != null ? latestPostPlace.getImageUrl()
                                                                        : null)
                                                        .viewCount(defaultZero(place.getViewCount()))
                                                        .build();
                                })
                                .toList();

                return ArchiverHotPlaceDto.ListResponse.from(cards);
        }

        public EditorUploadedPlaceDto.ListResponse getUploadedPlaces(UUID editorId) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorUploadedPlaceDto.ListResponse.empty();
                }

                Map<UUID, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<UUID, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorUploadedPlaceDto.PlaceCardResponse> places = postPlacesByPlaceId.entrySet().stream()
                                .sorted(Comparator.comparing(
                                                (Map.Entry<UUID, List<PostPlace>> entry) -> getLatestUpdatedAt(
                                                                entry.getValue()),
                                                Comparator.nullsLast(Comparator.naturalOrder()))
                                                .reversed())
                                .map(entry -> toPlaceCardResponse(entry.getKey(), entry.getValue(), placeMap))
                                .toList();

                return EditorUploadedPlaceDto.ListResponse.from(places);
        }

        private EditorUploadedPlaceDto.PlaceCardResponse toPlaceCardResponse(
                        UUID placeId,
                        List<PostPlace> postPlaces,
                        Map<UUID, Place> placeMap) {
                PostPlace latestPostPlace = postPlaces.stream()
                                .max(Comparator.comparing(this::getLastUpdatedAt,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .orElseThrow();

                Place place = placeMap.get(placeId);
                return EditorUploadedPlaceDto.PlaceCardResponse.of(
                                placeId,
                                place != null ? place.getName() : null,
                                latestPostPlace.getImageUrl(),
                                latestPostPlace.getDescription(),
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

        public ArchiverPlaceDetailDto.Response getArchiverPlaceDetail(UUID placeId) {
                Place place = placeRepository.findById(placeId)
                                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

                List<PostPlace> postPlaces = postPlaceRepository.findAllByPlaceId(placeId);

                ArchiverPlaceDetailDto.PlaceResponse placeResponse = ArchiverPlaceDetailDto.PlaceResponse.builder()
                                .placeId(place.getId())
                                .name(place.getName())
                                .roadAddress(place.getAddress() != null ? place.getAddress().getRoadAddress() : null)
                                .detailAddress(place.getAddress() != null ? place.getAddress().getDetailAddress() : null)
                                .zipCode(place.getAddress() != null ? place.getAddress().getZipCode() : null)
                                .latitude(place.getPosition() != null ? place.getPosition().getLatitude() : null)
                                .longitude(place.getPosition() != null ? place.getPosition().getLongitude() : null)
                                .nearestStationWalkTime(place.getNearestStationWalkTime())
                                .viewCount(defaultZero(place.getViewCount()))
                                .build();

                if (postPlaces.isEmpty()) {
                        return ArchiverPlaceDetailDto.Response.empty(placeResponse);
                }

                List<ArchiverPlaceDetailDto.PostPlaceResponse> postPlaceResponses = postPlaces.stream()
                                .map(postPlace -> ArchiverPlaceDetailDto.PostPlaceResponse.builder()
                                                .postPlaceId(postPlace.getId())
                                                .postId(postPlace.getPost() != null ? postPlace.getPost().getId() : null)
                                                .description(postPlace.getDescription())
                                                .imageUrl(postPlace.getImageUrl())
                                                .categoryNames(postPlace.getPostPlaceCategories().stream()
                                                                .map(PostPlaceCategory::getCategory)
                                                                .filter(category -> category != null)
                                                                .map(category -> category.getName())
                                                                .filter(name -> name != null)
                                                                .toList())
                                                .build())
                                .toList();

                return ArchiverPlaceDetailDto.Response.from(placeResponse, postPlaceResponses);
        }

        public EditorMapDto.Response getMapPins(
                        UUID editorId,
                        MapFilter filter,
                        List<UUID> categoryIds) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorId(editorId);
                if (postPlaces.isEmpty()) {
                        return EditorMapDto.Response.builder()
                                        .pins(List.of())
                                        .build();
                }

                Map<UUID, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<UUID, Place> placeMap = placeRepository.findAllByIds(
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

                return EditorInsightDto.SummaryResponse.of(
                                editorProfile.getNickname(),
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

                Map<UUID, List<PostPlace>> postPlacesByPlaceId = postPlaces.stream()
                                .collect(Collectors.groupingBy(pp -> pp.getPlace().getId()));

                Map<UUID, Place> placeMap = placeRepository.findAllByIds(
                                postPlacesByPlaceId.keySet().stream().toList())
                                .stream()
                                .collect(Collectors.toMap(Place::getId, Function.identity()));

                List<EditorInsightDto.PlaceCardResponse> places = postPlacesByPlaceId.entrySet().stream()
                                .map(entry -> toInsightPlaceCardResponse(entry.getKey(), entry.getValue(), placeMap))
                                .sorted(getInsightComparator(sort))
                                .toList();

                return EditorInsightDto.PlaceCardListResponse.of(sort, places);
        }

        public EditorInsightDto.PlaceDetailResponse getInsightPlaceDetail(UUID editorId, UUID placeId) {
                List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorIdAndPlaceId(editorId, placeId);
                if (postPlaces.isEmpty()) {
                        return EditorInsightDto.PlaceDetailResponse.empty(placeId);
                }

                EditorProfile editorProfile = editorProfileRepository.findByUserId(editorId)
                                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

                List<EditorInsightDto.PostPlaceDetailResponse> details = postPlaces.stream()
                                .map(postPlace -> {
                                        Post post = postPlace.getPost();
                                        List<String> categories = postPlace.getPostPlaceCategories().stream()
                                                        .map(pc -> pc.getCategory().getName())
                                                        .toList();
                                        return EditorInsightDto.PostPlaceDetailResponse.of(
                                                        editorProfile.getNickname(),
                                                        editorProfile.getInstagramId(),
                                                        post != null ? post.getUrl() : null,
                                                        post != null ? post.getHashTag() : null,
                                                        postPlace.getDescription(),
                                                        categories);
                                })
                                .toList();

                return EditorInsightDto.PlaceDetailResponse.of(placeId, details);
        }

        private EditorMapDto.PlacePinResponse toPlacePin(
                        UUID placeId,
                        List<PostPlace> postPlaces,
                        Map<UUID, Place> placeMap) {
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
                        MapFilter filter) {
                if (filter == null || filter == MapFilter.ALL) {
                        return true;
                }
                // NEARBY 필터일 때도 현재는 모든 핀을 반환하도록 설정 (추후 확장 가능)
                return true;
        }

        private boolean matchCategories(List<PostPlace> postPlaces, List<UUID> categoryIds) {
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
                        UUID placeId,
                        List<PostPlace> postPlaces,
                        Map<UUID, Place> placeMap) {
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

                return EditorInsightDto.PlaceCardResponse.of(
                                place,
                                latestPostPlace.getDescription(),
                                latestPostPlace.getImageUrl(),
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
