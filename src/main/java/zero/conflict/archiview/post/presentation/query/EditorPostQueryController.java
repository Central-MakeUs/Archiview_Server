package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.presentation.query.dto.EditorInsightDto;
import zero.conflict.archiview.post.presentation.query.dto.EditorMapDto;
import zero.conflict.archiview.post.presentation.query.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.presentation.query.dto.EditorMapDto.MapFilter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/editors")
@Tag(name = "Editor Post Query", description = "에디터 전용 및 관련 조회 API")
@RequiredArgsConstructor
public class EditorPostQueryController {

    private final PostQueryService postQueryService;

    @Operation(summary = "에디터 인사이트 요약 조회", description = "에디터 인사이트 요약 지표를 조회합니다.")
    @GetMapping("/me/insights/summary")
    public ResponseEntity<EditorInsightDto.SummaryResponse> getInsightSummary(
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(EditorInsightDto.SummaryResponse.empty(period));
    }

    @Operation(summary = "에디터 인사이트 장소 목록 조회", description = "에디터 인사이트 장소 목록을 조회합니다.")
    @GetMapping("/me/insights/places")
    public ResponseEntity<EditorInsightDto.PlaceCardListResponse> getInsightPlaces(
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @RequestParam(defaultValue = "RECENT") EditorInsightDto.PlaceSort sort,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(EditorInsightDto.PlaceCardListResponse.empty(period, sort));
    }

    @Operation(summary = "에디터 장소 상세 조회", description = "에디터 인사이트 장소 상세를 조회합니다.")
    @GetMapping("/me/insights/places/{placeId}")
    public ResponseEntity<EditorInsightDto.PlaceDetailResponse> getInsightPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(EditorInsightDto.PlaceDetailResponse.empty(placeId, period));
    }

    @Operation(summary = "내 지도 장소 핀 조회", description = "에디터가 등록한 장소들을 지도 핀 형태로 조회합니다.")
    @GetMapping("/me/map/places")
    public ResponseEntity<EditorMapDto.Response> getMapPins(
            @RequestParam(defaultValue = "ALL") MapFilter filter,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double minLon,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double maxLon,
            @RequestParam(required = false) List<Long> categoryIds,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        return ResponseEntity.ok(
                postQueryService.getMapPins(
                        oAuth2User.getUserId(),
                        filter,
                        minLat,
                        minLon,
                        maxLat,
                        maxLon,
                        categoryIds));
    }

    @Operation(summary = "내가 업로드한 장소 목록 조회", description = "에디터가 등록한 장소 목록과 통계를 조회합니다.")
    @GetMapping("/me/places")
    public ResponseEntity<EditorUploadedPlaceDto.ListResponse> getUploadedPlaces(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(postQueryService.getUploadedPlaces(oAuth2User.getUserId()));
    }

}
