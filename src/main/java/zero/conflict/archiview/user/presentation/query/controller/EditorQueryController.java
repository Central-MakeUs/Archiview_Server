package zero.conflict.archiview.user.presentation.query.controller;

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
import zero.conflict.archiview.user.presentation.query.api.EditorQueryApi;

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
@Validated
public class EditorQueryController implements EditorQueryApi {

    private final EditorUserUseCase editorUserUseCase;

    @Override
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> getMyProfile(
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorProfileDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                editorUserUseCase.getMyProfile(oAuth2User.getUserId())));
    }
    @Override
    @GetMapping("/profile/instagram-id/exists")
    public ResponseEntity<ApiResponse<EditorProfileDto.InstagramIdCheckResponse>> checkInstagramId(
            @RequestParam(defaultValue = "false") boolean useMock,
            @RequestParam String instagramId) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(
                    EditorProfileDto.InstagramIdCheckResponse.of(true)));
        }
        boolean exists = editorUserUseCase.existsInstagramId(instagramId);
        return ResponseEntity.ok(ApiResponse.success(
                EditorProfileDto.InstagramIdCheckResponse.of(exists)));
    }

    @Override
    @GetMapping("/profile/nickname/exists")
    public ResponseEntity<ApiResponse<EditorProfileDto.NicknameCheckResponse>> checkNickname(
            @RequestParam(defaultValue = "false") boolean useMock,
            @RequestParam String nickname) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(
                    EditorProfileDto.NicknameCheckResponse.of(true)));
        }
        boolean exists = editorUserUseCase.existsNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success(
                EditorProfileDto.NicknameCheckResponse.of(exists)));
    }
}
