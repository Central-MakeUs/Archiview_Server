package zero.conflict.archiview.post.application.port.in;

import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.ArchiverSavedPostPlaceDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.util.List;
import java.util.UUID;

public interface ArchiverPostUseCase {

    ArchiverHotPlaceDto.ListResponse getHotPlaces(int size, UUID archiverId);

    ArchiverPlaceDetailDto.Response getArchiverPlaceDetail(Long placeId, UUID archiverId);

    CategoryQueryDto.CategoryPlaceListResponse getNearbyPlacesWithin1km(Double latitude, Double longitude, UUID archiverId);

    ArchiverEditorPostPlaceDto.ListResponse getEditorUploadedPostPlaces(
            UUID userId,
            ArchiverEditorPostPlaceDto.Sort sort,
            UUID archiverId);

    EditorMapDto.Response getMapPinsForArchiver(
            UUID editorId,
            EditorMapDto.MapFilter filter,
            List<Long> categoryIds,
            Double latitude,
            Double longitude,
            UUID archiverId);

    CategoryQueryDto.CategoryListResponse getCategories();

    CategoryQueryDto.CategoryPlaceListResponse getPlacesByCategoryId(Long categoryId, UUID archiverId);

    void reportPostPlace(UUID archiverId, Long postPlaceId);

    void cancelReportPostPlace(UUID archiverId, Long postPlaceId);

    void savePostPlace(UUID archiverId, Long postPlaceId);

    void unsavePostPlace(UUID archiverId, Long postPlaceId);

    ArchiverSavedPostPlaceDto.ListResponse getMySavedPostPlaces(UUID archiverId);

    EditorMapDto.Response getMySavedMapPins(
            EditorMapDto.MapFilter filter,
            List<Long> categoryIds,
            Double latitude,
            Double longitude,
            UUID archiverId);

    void increasePostPlaceViewCount(Long postPlaceId, UUID actorId);
}
