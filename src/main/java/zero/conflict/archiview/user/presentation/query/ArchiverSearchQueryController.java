package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.query.ArchiverSearchQueryService;
import zero.conflict.archiview.user.dto.SearchDto;

@Tag(name = "Archiver Place Query", description = "아카이버 검색 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/search")
public class ArchiverSearchQueryController {

    private final ArchiverSearchQueryService archiverSearchQueryService;

    @Operation(summary = "아카이버 장소 검색", description = "전체/정보/에디터 탭 기반 검색 결과를 조회합니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<SearchDto.Response>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "ALL") SearchDto.Tab tab,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                archiverSearchQueryService.search(oAuth2User.getUserId(), q, tab)));
    }

    @Operation(summary = "최근 검색어 조회", description = "최근 검색어 최대 7개를 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<SearchDto.RecentListResponse>> getRecentSearches(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                archiverSearchQueryService.getRecentSearches(oAuth2User.getUserId())));
    }

    @Operation(summary = "최근 검색어 삭제", description = "최근 검색어를 삭제합니다.")
    @DeleteMapping("/recent/{historyId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecentSearch(
            @PathVariable Long historyId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverSearchQueryService.deleteRecentSearch(oAuth2User.getUserId(), historyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "추천 키워드 조회", description = "추천 키워드 최대 7개를 조회합니다.")
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<SearchDto.RecommendationListResponse>> getRecommendations(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                archiverSearchQueryService.getRecommendations(oAuth2User.getUserId())));
    }
}
