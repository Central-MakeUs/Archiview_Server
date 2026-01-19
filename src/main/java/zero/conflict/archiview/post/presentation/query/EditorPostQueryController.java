package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.user.presentation.dto.EditorInsightDto;

@RestController
@RequestMapping("/editors/me/insights")
@Tag(name = "Editor Post Query", description = "에디터 게시글/장소 조회 API")
public class EditorPostQueryController {

    @Operation(summary = "에디터 인사이트 요약 조회", description = "에디터 인사이트 요약 지표를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<EditorInsightDto.SummaryResponse> getInsightSummary(
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(EditorInsightDto.SummaryResponse.empty(period));
    }

    @Operation(summary = "에디터 인사이트 장소 목록 조회", description = "에디터 인사이트 장소 목록을 조회합니다.")
    @GetMapping("/places")
    public ResponseEntity<EditorInsightDto.PlaceCardListResponse> getInsightPlaces(
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @RequestParam(defaultValue = "RECENT") EditorInsightDto.PlaceSort sort,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(EditorInsightDto.PlaceCardListResponse.empty(period, sort));
    }

    @Operation(summary = "에디터 장소 상세 조회", description = "에디터 인사이트 장소 상세를 조회합니다.")
    @GetMapping("/places/{placeId}")
    public ResponseEntity<EditorInsightDto.PlaceDetailResponse> getInsightPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        return ResponseEntity.ok(EditorInsightDto.PlaceDetailResponse.empty(placeId, period));
    }
}
