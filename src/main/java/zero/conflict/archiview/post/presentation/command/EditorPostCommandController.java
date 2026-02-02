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
import zero.conflict.archiview.post.application.command.PostCommandService;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;
import zero.conflict.archiview.post.application.command.dto.PresignedUrlCommandDto;

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Tag(name = "Editor Post Command", description = "에디터용 장소 정보 업데이트 API (CUD)")
public class EditorPostCommandController {

    private final PostCommandService postCommandService;

    @Operation(summary = "게시글(장소) 등록", description = "에디터가 새로운 장소 정보를 포함한 게시글을 등록합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(encoding = @io.swagger.v3.oas.annotations.media.Encoding(name = "request", contentType = "application/json")))
    @PostMapping(value = "/posts", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostCommandDto.Response>> createPost(
            @RequestPart("request") @Valid PostCommandDto.Request request,
            @RequestPart("images") java.util.List<org.springframework.web.multipart.MultipartFile> images,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(ApiResponse.success(postCommandService.createPost(request, images, user.getUserId())));
    }

    @Operation(summary = "게시글 이미지 presigned URL 발급", description = "에디터가 게시글 이미지 업로드를 위한 presigned URL을 발급받습니다.")
    @PostMapping("/posts/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlCommandDto.Response>> createPostImagePresignedUrl(
            @RequestBody @Valid PresignedUrlCommandDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(postCommandService.createPostImagePresignedUrl(request)));
    }

}
