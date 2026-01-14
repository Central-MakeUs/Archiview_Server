package zero.conflict.archiview.post.presentation.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.post.application.command.PostCommandService;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;

@RestController
@RequiredArgsConstructor
public class PostCommandController {

    private PostCommandService postCommandService;

    @PostMapping("/posts")
    public ResponseEntity<PostCommandDto.Response> createPost(
            @RequestBody PostCommandDto.Request request,
            @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(postCommandService.createPost(request, user.getUserId()));
    }
}
