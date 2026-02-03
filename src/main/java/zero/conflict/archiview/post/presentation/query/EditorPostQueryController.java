package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/editors")
@Tag(name = "Editor Post Query", description = "에디터용 장소 정보 관련 조회 API")
@RequiredArgsConstructor
public class EditorPostQueryController {

    private final PostQueryService postQueryService;

    @Operation(summary = "에디터 인사이트 요약 조회", description = "에디터 인사이트 요약 지표를 조회합니다.")
    @GetMapping("/me/insights/summary")
    public ResponseEntity<ApiResponse<EditorInsightDto.SummaryResponse>> getInsightSummary(
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorInsightDto.SummaryResponse.mock(period)));
        }
        return ResponseEntity.ok(ApiResponse.success(postQueryService.getInsightSummary(user.getUserId(), period)));
    }

    @Operation(summary = "에디터 인사이트 장소 목록 조회", description = "에디터 인사이트 장소 목록을 조회합니다.")
    @GetMapping("/me/insights/places")
    public ResponseEntity<ApiResponse<EditorInsightDto.PlaceCardListResponse>> getInsightPlaces(
            @RequestParam(defaultValue = "RECENT") EditorInsightDto.PlaceSort sort,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorInsightDto.PlaceCardListResponse.mock(sort)));
        }
        return ResponseEntity.ok(ApiResponse.success(postQueryService.getInsightPlaces(user.getUserId(), sort)));
    }

    @Operation(summary = "에디터 장소 상세 조회", description = "에디터 인사이트 장소 상세를 조회합니다.")
    @GetMapping("/me/insights/places/{placeId}")
    public ResponseEntity<ApiResponse<EditorInsightDto.PlaceDetailResponse>> getInsightPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorInsightDto.PlaceDetailResponse.mock(placeId)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                postQueryService.getInsightPlaceDetail(user.getUserId(), placeId)));
    }

    @Operation(summary = "내 장소 지도 핀 조회", description = "에디터가 등록한 장소들을 지도 핀 형태로 조회합니다.")
    @GetMapping("/me/map/places")
    public ResponseEntity<ApiResponse<EditorMapDto.Response>> getMapPins(
            @RequestParam(defaultValue = "ALL") MapFilter filter,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorMapDto.Response.mock()));
        }

        return ResponseEntity.ok(ApiResponse.success(
                postQueryService.getMapPins(
                        oAuth2User.getUserId(),
                        filter,
                        categoryIds)));
    }

    @Operation(summary = "내가 업로드한 장소 목록 조회", description = "에디터가 등록한 장소 목록과 통계를 조회합니다.")
    @GetMapping("/me/places")
    public ResponseEntity<ApiResponse<EditorUploadedPlaceDto.ListResponse>> getUploadedPlaces(
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorUploadedPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(postQueryService.getUploadedPlaces(oAuth2User.getUserId())));
    }

}
