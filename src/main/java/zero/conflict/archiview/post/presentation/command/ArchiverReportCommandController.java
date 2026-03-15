package zero.conflict.archiview.post.presentation.command;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/archivers/reports/post-places")
public class ArchiverReportCommandController implements ArchiverReportCommandApi {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Override
    @PostMapping("/{postPlaceId}")
    public ResponseEntity<ApiResponse<Void>> reportPostPlace(
            @PathVariable Long postPlaceId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverPostUseCase.reportPostPlace(oAuth2User.getUserId(), postPlaceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Override
    @DeleteMapping("/{postPlaceId}")
    public ResponseEntity<ApiResponse<Void>> cancelReportPostPlace(
            @PathVariable Long postPlaceId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverPostUseCase.cancelReportPostPlace(oAuth2User.getUserId(), postPlaceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
