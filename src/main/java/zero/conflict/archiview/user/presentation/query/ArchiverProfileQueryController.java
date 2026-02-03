package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.query.ArchiverProfileQueryService;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;

@Tag(name = "Archiver Profile Query", description = "아카이버 프로필 관련 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverProfileQueryController {

    private final ArchiverProfileQueryService archiverProfileQueryService;

    @Operation(summary = "내 프로필 조회 (아카이버)", description = "로그인한 아카이버 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<ArchiverProfileDto.Response>> getMyProfile(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverProfileDto.Response.mock()));
        }
        return ResponseEntity
                .ok(ApiResponse.success(archiverProfileQueryService.getMyProfile(oAuth2User.getUserId())));
    }

}
