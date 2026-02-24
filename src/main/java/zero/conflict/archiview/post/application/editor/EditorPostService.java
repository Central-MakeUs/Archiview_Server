package zero.conflict.archiview.post.application.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.editor.command.PostCommandService;
import zero.conflict.archiview.post.application.editor.query.EditorPostQueryService;
import zero.conflict.archiview.post.application.port.in.EditorPostUseCase;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto.PlaceSort;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EditorPostService implements EditorPostUseCase {

    private final PostCommandService postCommandService;
    private final EditorPostQueryService editorPostQueryService;

    @Override
    @Transactional
    public PostCommandDto.Response createPost(PostCommandDto.CreateRequest request, UUID editorId) {
        return postCommandService.createPost(request, editorId);
    }

    @Override
    @Transactional
    public PresignedUrlCommandDto.Response createPostImagePresignedUrl(PresignedUrlCommandDto.Request request) {
        return postCommandService.createPostImagePresignedUrl(request);
    }

    @Override
    @Transactional
    public PostCommandDto.Response updatePost(Long postId, PostCommandDto.UpdateRequest request, UUID editorId) {
        return postCommandService.updatePost(postId, request, editorId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, UUID editorId) {
        postCommandService.deletePost(postId, editorId);
    }

    @Override
    public EditorInsightDto.SummaryResponse getInsightSummary(UUID editorId, EditorInsightDto.Period period) {
        return editorPostQueryService.getInsightSummary(editorId, period);
    }

    @Override
    public EditorInsightDto.PlaceCardListResponse getInsightPlaces(UUID editorId, EditorInsightDto.PlaceSort sort) {
        return editorPostQueryService.getInsightPlaces(editorId, sort);
    }

    @Override
    public EditorInsightDto.PlaceDetailResponse getInsightPlaceDetail(UUID editorId, Long placeId) {
        return editorPostQueryService.getInsightPlaceDetail(editorId, placeId);
    }

    @Override
    public EditorMapDto.Response getMapPins(
            UUID editorId,
            EditorMapDto.MapFilter filter,
            Long categoryId,
            Double latitude,
            Double longitude) {
        return editorPostQueryService.getMapPins(editorId, filter, categoryId, latitude, longitude);
    }

    @Override
    public EditorUploadedPlaceDto.ListResponse getUploadedPlaces(
            UUID editorId,
            EditorMapDto.MapFilter filter,
            EditorUploadedPlaceDto.PlaceSort sort,
            Long categoryId,
            Double latitude,
            Double longitude) {
        return editorPostQueryService.getUploadedPlaces(editorId, filter, sort, categoryId, latitude, longitude);
    }

    @Override
    public EditorPostByPostPlaceDto.Response getPostByPostPlaceId(Long postPlaceId) {
        return editorPostQueryService.getPostByPostPlaceId(postPlaceId);
    }
}
