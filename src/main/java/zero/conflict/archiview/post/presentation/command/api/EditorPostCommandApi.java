package zero.conflict.archiview.post.presentation.command.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;

@Tag(name = "Editor Place Command", description = "에디터용 장소 정보 업데이트 API (CUD)")
public interface EditorPostCommandApi {

    @Operation(summary = "게시글(장소) 등록", description = "에디터가 새로운 장소 정보를 포함한 게시글을 등록합니다.")
    ResponseEntity<ApiResponse<PostCommandDto.Response>> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "게시글 생성 요청")
            @Valid PostCommandDto.CreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "인스타그램 게시글 자동완성 미리보기",
            description = "인스타그램 게시글 URL로 게시글 등록 자동완성에 필요한 caption, hashtag, image 정보를 추출합니다.")
    ResponseEntity<ApiResponse<InstagramPreviewDto.Response>> previewInstagramPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "인스타그램 게시글 URL 1개를 포함한 자동완성 요청")
            @Valid InstagramPreviewDto.Request request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "게시글 이미지 presigned URL 발급", description = "에디터가 게시글 이미지 업로드를 위한 presigned URL을 발급받습니다.")
    ResponseEntity<ApiResponse<PresignedUrlCommandDto.Response>> createPostImagePresignedUrl(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "presigned URL 발급 요청")
            @Valid PresignedUrlCommandDto.Request request);

    @Operation(summary = "게시글 수정", description = "에디터가 등록한 게시글 정보를 수정합니다.")
    ResponseEntity<ApiResponse<PostCommandDto.Response>> updatePost(
            @Parameter(description = "수정할 post ID", example = "1") Long postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "게시글 수정 요청")
            @Valid PostCommandDto.UpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "게시글 삭제", description = "에디터가 등록한 게시글을 삭제합니다.")
    ResponseEntity<ApiResponse<Void>> deletePost(
            @Parameter(description = "삭제할 post ID", example = "1") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);
}
