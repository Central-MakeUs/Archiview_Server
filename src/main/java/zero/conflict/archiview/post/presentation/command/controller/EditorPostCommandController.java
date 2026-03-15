package zero.conflict.archiview.post.presentation.command.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.port.in.EditorPostUseCase;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;
import zero.conflict.archiview.post.presentation.command.api.EditorPostCommandApi;

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
public class EditorPostCommandController implements EditorPostCommandApi {

    private final EditorPostUseCase editorPostUseCase;

    @Override
    @PostMapping(value = "/posts", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostCommandDto.Response>> createPost(
            @RequestBody @Valid PostCommandDto.CreateRequest request,
            @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.createPost(request, user.getUserId())));
    }

    @Override
    @PostMapping(value = "/posts/instagram-preview", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InstagramPreviewDto.Response>> previewInstagramPost(
            @RequestBody @Valid InstagramPreviewDto.Request request,
            @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.previewInstagramPost(request, user.getUserId())));
    }

    @Override
    @PostMapping("/posts/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlCommandDto.Response>> createPostImagePresignedUrl(
            @RequestBody @Valid PresignedUrlCommandDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.createPostImagePresignedUrl(request)));
    }

    @Override
    @PutMapping(value = "/me/posts/{postId}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostCommandDto.Response>> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostCommandDto.UpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(ApiResponse.success(
                editorPostUseCase.updatePost(postId, request, user.getUserId())));
    }

    @Override
    @DeleteMapping("/me/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomOAuth2User user) {
        editorPostUseCase.deletePost(postId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
