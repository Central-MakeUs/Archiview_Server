package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.query.ArchiverProfileQueryService;
import zero.conflict.archiview.user.application.query.FollowQueryService;
import zero.conflict.archiview.user.application.query.TrustedEditorQueryService;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

@Tag(name = "Archiver Query", description = "아카이버 전용 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverQueryController {

    private final ArchiverProfileQueryService archiverProfileQueryService;
    private final FollowQueryService followQueryService;
    private final TrustedEditorQueryService trustedEditorQueryService;

    @Operation(summary = "내 프로필 조회 (아카이버)", description = "로그인한 아카이버 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<ArchiverProfileDto.Response>> getMyProfile(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverProfileDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverProfileQueryService.getMyProfile(oAuth2User.getUserId())));
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
                followQueryService.getMyFollowings(oAuth2User.getUserId())));
    }

    @Operation(summary = "믿고 먹는 에디터 조회", description = "팔로워 수와 등록한 포스트 장소 수를 기준으로 상위 에디터를 조회합니다.")
    @GetMapping("/editors/trusted")
    public ResponseEntity<ApiResponse<TrustedEditorDto.ListResponse>> getTrustedEditors(
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(TrustedEditorDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(trustedEditorQueryService.getTrustedEditors()));
    }
}
