package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.dto.SearchDto;

@Tag(name = "Archiver Search Query", description = "아카이버 검색 조회 API")
public interface ArchiverSearchQueryApi {

    @Operation(summary = "아카이버 장소 검색",
            description = "검색어와 탭 기준으로 장소/정보/에디터 검색 결과를 조회합니다.")
    ResponseEntity<ApiResponse<SearchDto.Response>> search(
            @Parameter(description = "검색어", example = "성수") String q,
            @Parameter(description = "검색 탭. ALL, PLACE, INFORMATION, EDITOR 중 하나", example = "ALL") SearchDto.Tab tab,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "최근 검색어 조회",
            description = "로그인한 아카이버의 최근 검색어를 최대 7개까지 조회합니다.")
    ResponseEntity<ApiResponse<SearchDto.RecentListResponse>> getRecentSearches(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "최근 검색어 삭제",
            description = "최근 검색어 1건을 삭제합니다.")
    ResponseEntity<ApiResponse<Void>> deleteRecentSearch(
            @Parameter(description = "삭제할 검색 히스토리 ID", example = "1") Long historyId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "추천 키워드 조회",
            description = "로그인한 아카이버 기준 추천 키워드 목록을 최대 7개까지 조회합니다.")
    ResponseEntity<ApiResponse<SearchDto.RecommendationListResponse>> getRecommendations(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
