package zero.conflict.archiview.post.presentation.query;

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
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.ArchiverSavedPostPlaceDto;

@Tag(name = "Archiver Place Query", description = "아카이버 저장한 장소카드 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/saves/post-places")
public class ArchiverSaveQueryController {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Operation(summary = "저장한 장소카드 목록 조회", description = "아카이버가 저장한 장소카드(postPlace) 목록을 최근 저장순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<ArchiverSavedPostPlaceDto.ListResponse>> getMySavedPostPlaces(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverSavedPostPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getMySavedPostPlaces(oAuth2User.getUserId())));
    }
}
