package zero.conflict.archiview.post.application.archiver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.editor.command.PostCommandService;
import zero.conflict.archiview.post.application.archiver.command.PostReportCommandService;
import zero.conflict.archiview.post.application.archiver.command.PostSaveCommandService;
import zero.conflict.archiview.post.application.archiver.query.CategoryQueryService;
import zero.conflict.archiview.post.application.archiver.query.PostQueryService;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.ArchiverSavedPostPlaceDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchiverPostService implements ArchiverPostUseCase {

    private final PostQueryService postQueryService;
    private final CategoryQueryService categoryQueryService;
    private final PostReportCommandService postReportCommandService;
    private final PostSaveCommandService postSaveCommandService;
    private final PostCommandService postCommandService;

    @Override
    public ArchiverHotPlaceDto.ListResponse getHotPlaces(int size, UUID archiverId) {
        return postQueryService.getHotPlaces(size, archiverId);
    }

    @Override
    public ArchiverPlaceDetailDto.Response getArchiverPlaceDetail(Long placeId, UUID archiverId) {
        return postQueryService.getArchiverPlaceDetail(placeId, archiverId);
    }

    @Override
    public CategoryQueryDto.CategoryPlaceListResponse getNearbyPlacesWithin1km(
            Double latitude,
            Double longitude,
            UUID archiverId) {
        return postQueryService.getNearbyPlacesWithin1km(latitude, longitude, archiverId);
    }

    @Override
    public ArchiverEditorPostPlaceDto.ListResponse getEditorUploadedPostPlaces(
            UUID userId,
            ArchiverEditorPostPlaceDto.Sort sort,
            UUID archiverId) {
        return postQueryService.getEditorUploadedPostPlaces(userId, sort, archiverId);
    }

    @Override
    public EditorMapDto.Response getMapPinsForArchiver(
            UUID editorId,
            EditorMapDto.MapFilter filter,
            List<Long> categoryIds,
            Double latitude,
            Double longitude,
            UUID archiverId) {
        return postQueryService.getMapPinsForArchiver(
                editorId,
                filter,
                categoryIds,
                latitude,
                longitude,
                archiverId);
    }

    @Override
    public CategoryQueryDto.CategoryListResponse getCategories() {
        return categoryQueryService.getCategories();
    }

    @Override
    public CategoryQueryDto.CategoryPlaceListResponse getPlacesByCategoryId(Long categoryId, UUID archiverId) {
        return categoryQueryService.getPlacesByCategoryId(categoryId, archiverId);
    }

    @Override
    public void reportPostPlace(UUID archiverId, Long postPlaceId) {
        postReportCommandService.reportPostPlace(archiverId, postPlaceId);
    }

    @Override
    public void cancelReportPostPlace(UUID archiverId, Long postPlaceId) {
        postReportCommandService.cancelReportPostPlace(archiverId, postPlaceId);
    }

    @Override
    public void savePostPlace(UUID archiverId, Long postPlaceId) {
        postSaveCommandService.savePostPlace(archiverId, postPlaceId);
    }

    @Override
    public void unsavePostPlace(UUID archiverId, Long postPlaceId) {
        postSaveCommandService.unsavePostPlace(archiverId, postPlaceId);
    }

    @Override
    public ArchiverSavedPostPlaceDto.ListResponse getMySavedPostPlaces(UUID archiverId) {
        return postQueryService.getMySavedPostPlaces(archiverId);
    }

    @Override
    public EditorMapDto.Response getMySavedMapPins(
            EditorMapDto.MapFilter filter,
            List<Long> categoryIds,
            Double latitude,
            Double longitude,
            UUID archiverId) {
        return postQueryService.getMySavedMapPins(filter, categoryIds, latitude, longitude, archiverId);
    }

    @Override
    public void increasePostPlaceViewCount(Long postPlaceId, UUID actorId) {
        postCommandService.increasePostPlaceViewCount(postPlaceId, actorId);
    }
}
