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

@Tag(name = "Archiver Place Command", description = "아카이버 장소카드 저장 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/saves/post-places")
public class ArchiverSaveCommandController {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Operation(summary = "장소카드 저장", description = "아카이버가 장소카드(postPlace)를 저장합니다.")
    @PostMapping("/{postPlaceId}")
    public ResponseEntity<ApiResponse<Void>> savePostPlace(
            @PathVariable Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverPostUseCase.savePostPlace(oAuth2User.getUserId(), postPlaceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "장소카드 저장 해제", description = "아카이버가 저장한 장소카드(postPlace)를 해제합니다.")
    @DeleteMapping("/{postPlaceId}")
    public ResponseEntity<ApiResponse<Void>> unsavePostPlace(
            @PathVariable Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverPostUseCase.unsavePostPlace(oAuth2User.getUserId(), postPlaceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
