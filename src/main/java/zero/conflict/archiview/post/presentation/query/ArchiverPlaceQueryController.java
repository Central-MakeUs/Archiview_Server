package zero.conflict.archiview.post.presentation.query;

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
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Archiver Place Query", description = "아카이버용 핫플레이스 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceQueryController {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Operation(summary = "요즘 핫한 장소 조회", description = "장소 기준 조회수 내림차순으로 핫플레이스를 조회합니다.")
    @GetMapping("/places/hot")
    public ResponseEntity<ApiResponse<ArchiverHotPlaceDto.ListResponse>> getHotPlaces(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverHotPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getHotPlaces(size, oAuth2User.getUserId())));
    }

    @Operation(summary = "장소 상세 조회 (아카이버)", description = "placeId로 장소 상세와 연결된 게시글 목록을 조회합니다.")
    @GetMapping("/places/{placeId}")
    public ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverPlaceDetailDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getArchiverPlaceDetail(placeId, oAuth2User.getUserId())));
    }

    @Operation(summary = "특정 에디터가 업로드한 장소 상세 조회 (아카이버용)", description = "아카이버 장소 상세 응답 형식으로 특정 에디터가 업로드한 장소카드만 조회합니다.")
    @GetMapping("/editors/{editorId}/places/{placeId}")
    public ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetailByEditor(
            @PathVariable UUID editorId,
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverPlaceDetailDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getArchiverPlaceDetailByEditor(placeId, editorId, oAuth2User.getUserId())));
    }

    @Operation(summary = "내 주변 1km 장소 조회", description = "현재 좌표 기준 1km 내 장소 목록을 조회합니다.")
    @GetMapping("/places/nearby")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getNearbyPlaces(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryPlaceListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getNearbyPlacesWithin1km(
                latitude,
                longitude,
                oAuth2User.getUserId())));
    }

    @Operation(summary = "에디터 업로드 장소 목록 조회 (아카이버)", description = "아카이버가 특정 에디터가 업로드한 postPlace 목록을 조회합니다.")
    @GetMapping("/editors/{editorId}/post-places")
    public ResponseEntity<ApiResponse<ArchiverEditorPostPlaceDto.ListResponse>> getEditorUploadedPostPlaces(
            @PathVariable UUID editorId,
            @RequestParam(defaultValue = "LATEST") ArchiverEditorPostPlaceDto.Sort sort,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverEditorPostPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getEditorUploadedPostPlaces(
                editorId,
                sort,
                oAuth2User.getUserId())));
    }

    @Operation(summary = "에디터 업로드 장소 핀 지도 조회 (아카이버)", description = "아카이버가 특정 에디터의 장소 핀을 필터 조건으로 조회합니다.")
    @GetMapping("/editors/{editorId}/map/places")
    public ResponseEntity<ApiResponse<EditorMapDto.Response>> getEditorMapPins(
            @PathVariable UUID editorId,
            @RequestParam(defaultValue = "ALL") EditorMapDto.MapFilter filter,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorMapDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getMapPinsForArchiver(
                        editorId,
                        filter,
                        categoryId,
                        latitude,
                        longitude,
                        oAuth2User.getUserId())));
    }
}
