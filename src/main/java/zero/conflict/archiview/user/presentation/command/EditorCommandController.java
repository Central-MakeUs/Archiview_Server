package zero.conflict.archiview.user.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.command.EditorProfileCommandService;
import zero.conflict.archiview.user.dto.EditorProfileDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/editors")
@Tag(name = "Editor User Command", description = "에디터용 프로필 업데이트 관련 API")
public class EditorCommandController {

    private final EditorProfileCommandService editorProfileCommandService;

    @Operation(summary = "에디터 내 프로필 등록", description = "로그인한 사용자의 에디터 프로필을 등록합니다.")
    @PostMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> createMyProfile(
            @RequestBody @Valid EditorProfileDto.CreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                editorProfileCommandService.createProfile(oAuth2User.getUserId(), request)));
    }

    @Operation(summary = "에디터 내 프로필 수정", description = "로그인한 사용자의 에디터 프로필 정보를 수정합니다.")
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> updateMyProfile(
            @RequestBody @Valid EditorProfileDto.UpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                editorProfileCommandService.updateProfile(oAuth2User.getUserId(), request)));
    }
}
