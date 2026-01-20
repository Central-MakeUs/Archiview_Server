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
import zero.conflict.archiview.post.application.command.PostCommandService;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;
import zero.conflict.archiview.user.presentation.dto.EditorProfileDto;

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Tag(name = "Editor Post Command", description = "에디터 전용 관리 API (CUD)")
public class EditorPostCommandController {

    private final PostCommandService postCommandService;

    @Operation(summary = "게시글(장소) 등록", description = "에디터가 새로운 장소 정보를 포함한 게시글을 등록합니다.")
    @PostMapping("/posts")
    public ResponseEntity<PostCommandDto.Response> createPost(
            @RequestBody @Valid PostCommandDto.Request request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(postCommandService.createPost(request, user.getUserId()));
    }

    @Operation(summary = "내 프로필 수정 (에디터)", description = "로그인한 에디터 자신의 프로필 정보를 수정합니다.")
    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(
            @RequestBody @Valid EditorProfileDto.UpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        // TODO: Service 연동
        return ResponseEntity.ok().build();
    }
    /**
     * 게시글 수정
     */
    // @PutMapping("/{postId}")
    // public ResponseEntity<Void> updatePost(PostCommandDto.UpdateRequest request,
    // @PathVariable Long postId,
    // @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
    // return ResponseEntity.ok().build();
    // }
}
