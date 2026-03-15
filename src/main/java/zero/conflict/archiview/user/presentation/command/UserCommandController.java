package zero.conflict.archiview.user.presentation.command;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.editor.EditorUserUseCase;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.UserDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserCommandController implements UserCommandApi {

    private final EditorUserUseCase editorUserUseCase;

    @Override
    @PostMapping("/me/editor-profile")
    public ResponseEntity<ApiResponse<UserDto.RegisterEditorProfileResponse>> registerEditorProfile(
            @RequestBody @Valid EditorProfileDto.CreateRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                editorUserUseCase.registerEditorProfile(oAuth2User.getUserId(), request)));
    }

    @Override
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @RequestBody @Valid UserDto.OnboardingRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        editorUserUseCase.completeOnboarding(oAuth2User.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Override
    @PostMapping("/switch-role")
    public ResponseEntity<ApiResponse<UserDto.SwitchRoleResponse>> switchRole(
            @RequestBody @Valid UserDto.SwitchRoleRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(editorUserUseCase.switchRole(oAuth2User.getUserId(), request)));
    }

    @Override
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        editorUserUseCase.withdraw(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
