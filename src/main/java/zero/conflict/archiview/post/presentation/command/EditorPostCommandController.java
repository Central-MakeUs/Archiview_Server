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

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Tag(name = "Editor Post Command", description = "에디터용 장소 정보 업데이트 API (CUD)")
public class EditorPostCommandController {

    private final PostCommandService postCommandService;

    @Operation(summary = "게시글(장소) 등록", description = "에디터가 새로운 장소 정보를 포함한 게시글을 등록합니다.")
    @PostMapping("/posts")
    public ResponseEntity<PostCommandDto.Response> createPost(
            @RequestBody @Valid PostCommandDto.Request request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(postCommandService.createPost(request, user.getUserId()));
    }

}
