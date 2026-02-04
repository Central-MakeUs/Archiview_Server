package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.query.EditorProfileQueryService;
import zero.conflict.archiview.user.dto.EditorProfileDto;

@Tag(name = "Editor Query", description = "에디터 전용 조회 API")
@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Validated
public class EditorQueryController {

    private final EditorProfileQueryService editorProfileQueryService;

    @Operation(summary = "에디터 프로필 조회", description = "로그인한 에디터 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                editorProfileQueryService.getMyProfile(oAuth2User.getUserId())));
    }

    @Operation(summary = "에디터 공개 프로필 조회", description = "특정 에디터의 공개된 프로필 정보를 조회합니다.")
    @GetMapping("/{editorId}/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> getEditorProfile(
            @PathVariable java.util.UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                editorProfileQueryService.getEditorProfile(editorId)));
    }

    @Operation(summary = "에디터 인스타그램 ID 중복 확인", description = "에디터 프로필 등록을 위한 인스타그램 ID 중복 여부를 확인합니다.")
    @GetMapping("/profile/instagram-id/exists")
    public ResponseEntity<ApiResponse<EditorProfileDto.InstagramIdCheckResponse>> checkInstagramId(
            @RequestParam @jakarta.validation.constraints.NotBlank String instagramId) {
        boolean exists = editorProfileQueryService.existsInstagramId(instagramId);
        return ResponseEntity.ok(ApiResponse.success(
                EditorProfileDto.InstagramIdCheckResponse.of(exists)));
    }
}
