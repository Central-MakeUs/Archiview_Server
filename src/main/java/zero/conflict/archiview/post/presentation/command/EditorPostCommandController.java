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

@Tag(name = "Editor Post Command", description = "에디터 게시글 등록·수정·삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class EditorPostCommandController {

    private final PostCommandService postCommandService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다")
    @PostMapping("")
    public ResponseEntity<PostCommandDto.Response> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "placeInfoRequestList[].categoryIds는 /categories 조회로 받은 ID를 사용합니다.")
            @RequestBody @Valid PostCommandDto.Request request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(postCommandService.createPost(request, user.getUserId()));
    }

    /**
     * 게시글 수정
     */
//    @PutMapping("/{postId}")
//    public ResponseEntity<Void> updatePost(PostCommandDto.UpdateRequest request,
//                                           @PathVariable Long postId,
//                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
//        return ResponseEntity.ok().build();
//    }
}
