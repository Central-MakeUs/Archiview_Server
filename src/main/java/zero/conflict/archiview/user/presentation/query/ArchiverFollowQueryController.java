package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.query.FollowQueryService;
import zero.conflict.archiview.user.dto.FollowDto;

@Tag(name = "Follow", description = "아카이버 팔로우 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/follows")
public class ArchiverFollowQueryController {

    private final FollowQueryService followQueryService;

    @Operation(summary = "내 팔로우 목록", description = "아카이버의 팔로잉 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<FollowDto.ListResponse>> getMyFollowings(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(FollowDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                followQueryService.getMyFollowings(oAuth2User.getUserId())));
    }
}
