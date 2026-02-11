package zero.conflict.archiview.post.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;

@Tag(name = "Archiver Place Command", description = "아카이버 게시글 신고 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/reports/post-places")
public class ArchiverReportCommandController {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Operation(summary = "장소카드 신고", description = "아카이버가 장소카드를 신고하고 이후 조회에서 숨깁니다.")
    @PostMapping("/{postPlaceId}")
    public ResponseEntity<ApiResponse<Void>> reportPostPlace(
            @PathVariable Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverPostUseCase.reportPostPlace(oAuth2User.getUserId(), postPlaceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "장소카드 신고 취소 (기획에 없는거지만 혹시 몰라서 API는 만들어둠)", description = "아카이버가 장소카드 신고를 취소합니다.")
    @DeleteMapping("/{postPlaceId}")
    public ResponseEntity<ApiResponse<Void>> cancelReportPostPlace(
            @PathVariable Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverPostUseCase.cancelReportPostPlace(oAuth2User.getUserId(), postPlaceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
