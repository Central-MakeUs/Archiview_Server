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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/editors")
public class EditorCommandController implements EditorCommandApi {

    private final EditorUserUseCase editorUserUseCase;

    @Override
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> updateMyProfile(
            @RequestBody @Valid EditorProfileDto.UpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                editorUserUseCase.updateProfile(oAuth2User.getUserId(), request)));
    }
}
