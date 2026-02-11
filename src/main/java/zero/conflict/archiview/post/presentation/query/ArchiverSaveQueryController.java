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
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.util.List;

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

    @Operation(summary = "저장한 장소 핀 지도 조회", description = "아카이버가 저장한 장소(postPlace 기준)의 지도 핀을 필터 조건으로 조회합니다.")
    @GetMapping("/map/places")
    public ResponseEntity<ApiResponse<EditorMapDto.Response>> getMySavedMapPins(
            @RequestParam(defaultValue = "ALL") EditorMapDto.MapFilter filter,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorMapDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getMySavedMapPins(
                        filter,
                        categoryIds,
                        latitude,
                        longitude,
                        oAuth2User.getUserId())));
    }
}
