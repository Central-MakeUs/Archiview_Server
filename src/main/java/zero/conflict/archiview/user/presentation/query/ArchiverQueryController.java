package zero.conflict.archiview.user.presentation.query;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.port.in.ArchiverUserUseCase;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.BlockDto;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverQueryController implements ArchiverQueryApi {

    private final ArchiverUserUseCase archiverUserUseCase;

    @Override
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<ArchiverProfileDto.Response>> getMyProfile(
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverProfileDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getMyProfile(oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/follows")
    public ResponseEntity<ApiResponse<FollowDto.ListResponse>> getMyFollowings(
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(FollowDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getMyFollowings(oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<BlockDto.ListResponse>> getMyBlockedEditors(
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(BlockDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getMyBlockedEditors(oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/editors/trusted")
    public ResponseEntity<ApiResponse<TrustedEditorDto.ListResponse>> getTrustedEditors(
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(TrustedEditorDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverUserUseCase.getTrustedEditors()));
    }

    @Override
    @GetMapping("/editors/{editorId}/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.ArchiverEditorProfileResponse>> getEditorProfile(
            @PathVariable java.util.UUID editorId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorProfileDto.ArchiverEditorProfileResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getEditorProfile(oAuth2User.getUserId(), editorId)));
    }
}
