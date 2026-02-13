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
import zero.conflict.archiview.user.application.editor.EditorUserUseCase;
import zero.conflict.archiview.user.dto.EditorProfileDto;

@Tag(name = "Editor User Query", description = "에디터 전용 조회 API")
@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Validated
public class EditorQueryController {

    private final EditorUserUseCase editorUserUseCase;

    @Operation(summary = "에디터 프로필 조회", description = "로그인한 에디터 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> getMyProfile(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorProfileDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                editorUserUseCase.getMyProfile(oAuth2User.getUserId())));
    }


    @Operation(summary = "에디터 인스타그램 ID 중복 확인", description = "에디터 프로필 등록을 위한 인스타그램 ID 중복 여부를 확인합니다.")
    @GetMapping("/profile/instagram-id/exists")
    public ResponseEntity<ApiResponse<EditorProfileDto.InstagramIdCheckResponse>> checkInstagramId(
            @RequestParam(defaultValue = "false") boolean useMock,
            @RequestParam @jakarta.validation.constraints.NotBlank String instagramId) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(
                    EditorProfileDto.InstagramIdCheckResponse.of(true)));
        }
        boolean exists = editorUserUseCase.existsInstagramId(instagramId);
        return ResponseEntity.ok(ApiResponse.success(
                EditorProfileDto.InstagramIdCheckResponse.of(exists)));
    }

    @Operation(summary = "에디터 닉네임 중복 확인", description = "에디터 프로필 등록을 위한 닉네임 중복 여부를 확인합니다.")
    @GetMapping("/profile/nickname/exists")
    public ResponseEntity<ApiResponse<EditorProfileDto.NicknameCheckResponse>> checkNickname(
            @RequestParam(defaultValue = "false") boolean useMock,
            @RequestParam @jakarta.validation.constraints.NotBlank String nickname) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(
                    EditorProfileDto.NicknameCheckResponse.of(true)));
        }
        boolean exists = editorUserUseCase.existsNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success(
                EditorProfileDto.NicknameCheckResponse.of(exists)));
    }
}
