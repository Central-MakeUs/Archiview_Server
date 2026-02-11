package zero.conflict.archiview.post.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.post.application.command.PostCommandService;
import zero.conflict.archiview.post.application.port.in.EditorPostUseCase;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorPostService implements EditorPostUseCase {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    @Override
    public PostCommandDto.Response createPost(PostCommandDto.Request request, UUID editorId) {
        return postCommandService.createPost(request, editorId);
    }

    @Override
    public PresignedUrlCommandDto.Response createPostImagePresignedUrl(PresignedUrlCommandDto.Request request) {
        return postCommandService.createPostImagePresignedUrl(request);
    }

    @Override
    public PostCommandDto.Response updatePost(Long postId, PostCommandDto.Request request, UUID editorId) {
        return postCommandService.updatePost(postId, request, editorId);
    }

    @Override
    public EditorInsightDto.SummaryResponse getInsightSummary(UUID editorId, EditorInsightDto.Period period) {
        return postQueryService.getInsightSummary(editorId, period);
    }

    @Override
    public EditorInsightDto.PlaceCardListResponse getInsightPlaces(UUID editorId, EditorInsightDto.PlaceSort sort) {
        return postQueryService.getInsightPlaces(editorId, sort);
    }

    @Override
    public EditorInsightDto.PlaceDetailResponse getInsightPlaceDetail(UUID editorId, Long placeId) {
        return postQueryService.getInsightPlaceDetail(editorId, placeId);
    }

    @Override
    public EditorMapDto.Response getMapPins(UUID editorId, EditorMapDto.MapFilter filter, List<Long> categoryIds) {
        return postQueryService.getMapPins(editorId, filter, categoryIds);
    }

    @Override
    public EditorUploadedPlaceDto.ListResponse getUploadedPlaces(UUID editorId) {
        return postQueryService.getUploadedPlaces(editorId);
    }

    @Override
    public EditorPostByPostPlaceDto.Response getPostByPostPlaceId(Long postPlaceId) {
        return postQueryService.getPostByPostPlaceId(postPlaceId);
    }
}
