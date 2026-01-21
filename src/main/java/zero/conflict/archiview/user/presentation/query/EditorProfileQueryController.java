package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.user.presentation.dto.EditorProfileDto;

@Tag(name = "Editor Profile Query", description = "에디터 프로필 조회 API")
@RestController
@RequestMapping("/api/v1/editors")
public class EditorProfileQueryController {

    @Operation(summary = "에디터 프로필 조회", description = "로그인한 에디터 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<EditorProfileDto.Response> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(EditorProfileDto.Response.builder().build());
    }

    @Operation(summary = "에디터 공개 프로필 조회", description = "특정 에디터의 공개된 프로필 정보를 조회합니다.")
    @GetMapping("/{editorId}/profile")
    public ResponseEntity<EditorProfileDto.Response> getEditorProfile(
            @PathVariable Long editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(EditorProfileDto.Response.builder().build());
    }
}
