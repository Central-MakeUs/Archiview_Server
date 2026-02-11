package zero.conflict.archiview.post.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.editor.EditorPostUseCase;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Tag(name = "Editor Place Command", description = "에디터용 장소 정보 업데이트 API (CUD)")
public class EditorPostCommandController {

    private final EditorPostUseCase editorPostUseCase;

    @Operation(summary = "게시글(장소) 등록", description = "에디터가 새로운 장소 정보를 포함한 게시글을 등록합니다.")
    @PostMapping(value = "/posts", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostCommandDto.Response>> createPost(
            @RequestBody @Valid PostCommandDto.Request request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.createPost(request, user.getUserId())));
    }

    @Operation(summary = "게시글 이미지 presigned URL 발급", description = "에디터가 게시글 이미지 업로드를 위한 presigned URL을 발급받습니다.")
    @PostMapping("/posts/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlCommandDto.Response>> createPostImagePresignedUrl(
            @RequestBody @Valid PresignedUrlCommandDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.createPostImagePresignedUrl(request)));
    }

    @Operation(summary = "게시글 수정", description = "에디터가 등록한 게시글 정보를 수정합니다.")
    @PutMapping(value = "/me/posts/{postId}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostCommandDto.Response>> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostCommandDto.Request request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(ApiResponse.success(
                editorPostUseCase.updatePost(postId, request, user.getUserId())));
    }

}
