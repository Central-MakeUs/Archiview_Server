package zero.conflict.archiview.user.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import zero.conflict.archiview.user.application.command.EditorBlockCommandService;

import java.util.UUID;

@Tag(name = "Archiver User Command", description = "아카이버 사용자 차단 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/blocks/editors")
public class ArchiverBlockCommandController {

    private final EditorBlockCommandService editorBlockCommandService;

    @Operation(summary = "에디터 차단", description = "아카이버가 에디터를 차단하고 해당 에디터의 게시글/장소카드를 숨깁니다.")
    @PostMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> blockEditor(
            @PathVariable UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        editorBlockCommandService.blockEditor(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "에디터 차단 해제", description = "아카이버가 에디터 차단을 해제합니다.")
    @DeleteMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> unblockEditor(
            @PathVariable UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        editorBlockCommandService.unblockEditor(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
