package zero.conflict.archiview.user.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.port.in.ArchiverUserUseCase;

import java.util.UUID;

@Tag(name = "Archiver User Command", description = "아카이버 팔로우 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/follows")
public class ArchiverFollowCommandController {

    private final ArchiverUserUseCase archiverUserUseCase;

    @Operation(summary = "팔로우 등록", description = "아카이버가 editorId 경로값의 에디터를 팔로우합니다.")
    @PostMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @PathVariable UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.follow(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "팔로우 취소", description = "아카이버가 에디터 팔로우를 취소합니다.")
    @DeleteMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.unfollow(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
