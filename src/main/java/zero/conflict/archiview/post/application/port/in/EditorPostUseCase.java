package zero.conflict.archiview.post.application.port.in;

import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;

import java.util.List;
import java.util.UUID;

public interface EditorPostUseCase {

    PostCommandDto.Response createPost(PostCommandDto.CreateRequest request, UUID editorId);

    PresignedUrlCommandDto.Response createPostImagePresignedUrl(PresignedUrlCommandDto.Request request);

    PostCommandDto.Response updatePost(Long postId, PostCommandDto.UpdateRequest request, UUID editorId);

    void deletePost(Long postId, UUID editorId);

    EditorInsightDto.SummaryResponse getInsightSummary(UUID editorId, EditorInsightDto.Period period);

    EditorInsightDto.PlaceCardListResponse getInsightPlaces(UUID editorId, EditorInsightDto.PlaceSort sort);

    EditorInsightDto.PlaceDetailResponse getInsightPlaceDetail(UUID editorId, Long placeId);

    EditorMapDto.Response getMapPins(
            UUID editorId,
            EditorMapDto.MapFilter filter,
            List<Long> categoryIds,
            Double latitude,
            Double longitude);

    EditorUploadedPlaceDto.ListResponse getUploadedPlaces(
            UUID editorId,
            EditorMapDto.MapFilter filter,
            EditorUploadedPlaceDto.PlaceSort sort,
            List<Long> categoryIds,
            Double latitude,
            Double longitude);

    EditorPostByPostPlaceDto.Response getPostByPostPlaceId(Long postPlaceId);
}
