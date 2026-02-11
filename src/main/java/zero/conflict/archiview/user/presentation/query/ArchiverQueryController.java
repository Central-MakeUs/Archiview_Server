package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import zero.conflict.archiview.user.application.archiver.ArchiverUserUseCase;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.BlockDto;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

@Tag(name = "Archiver User Query", description = "아카이버 전용 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverQueryController {

    private final ArchiverUserUseCase archiverUserUseCase;

    @Operation(summary = "내 프로필 조회 (아카이버)", description = "로그인한 아카이버 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<ArchiverProfileDto.Response>> getMyProfile(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverProfileDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getMyProfile(oAuth2User.getUserId())));
    }

    @Operation(summary = "내 팔로우 목록", description = "아카이버의 팔로잉 목록을 조회합니다.")
    @GetMapping("/follows")
    public ResponseEntity<ApiResponse<FollowDto.ListResponse>> getMyFollowings(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(FollowDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getMyFollowings(oAuth2User.getUserId())));
    }

    @Operation(summary = "내 차단 에디터 목록", description = "아카이버가 차단한 에디터 목록을 조회합니다.")
    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<BlockDto.ListResponse>> getMyBlockedEditors(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(BlockDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getMyBlockedEditors(oAuth2User.getUserId())));
    }

    @Operation(summary = "믿고 먹는 에디터 조회", description = "팔로워 수와 등록한 포스트 장소 수를 기준으로 상위 에디터를 조회합니다.")
    @GetMapping("/editors/trusted")
    public ResponseEntity<ApiResponse<TrustedEditorDto.ListResponse>> getTrustedEditors(
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(TrustedEditorDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverUserUseCase.getTrustedEditors()));
    }

    @Operation(summary = "에디터 프로필 조회 (아카이버)", description = "아카이버가 특정 에디터의 프로필 정보를 조회합니다.")
    @GetMapping("/editors/{editorId}/profile")
    public ResponseEntity<ApiResponse<EditorProfileDto.Response>> getEditorProfile(
            @PathVariable java.util.UUID editorId,
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorProfileDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverUserUseCase.getEditorProfile(editorId)));
    }
}
