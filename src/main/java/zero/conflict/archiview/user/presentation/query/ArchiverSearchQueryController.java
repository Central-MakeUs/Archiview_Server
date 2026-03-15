package zero.conflict.archiview.user.presentation.query;

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
import zero.conflict.archiview.user.application.port.in.ArchiverUserUseCase;
import zero.conflict.archiview.user.dto.SearchDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/search")
public class ArchiverSearchQueryController implements ArchiverSearchQueryApi {

    private final ArchiverUserUseCase archiverUserUseCase;

    @Override
    @GetMapping("")
    public ResponseEntity<ApiResponse<SearchDto.Response>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "ALL") SearchDto.Tab tab,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.search(oAuth2User.getUserId(), q, tab)));
    }

    @Override
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<SearchDto.RecentListResponse>> getRecentSearches(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getRecentSearches(oAuth2User.getUserId())));
    }

    @Override
    @DeleteMapping("/recent/{historyId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecentSearch(
            @PathVariable Long historyId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.deleteRecentSearch(oAuth2User.getUserId(), historyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Override
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<SearchDto.RecommendationListResponse>> getRecommendations(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(
                archiverUserUseCase.getRecommendations(oAuth2User.getUserId())));
    }
}
