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
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Query", description = "카테고리 조회 API")
public class CategoryQueryController {

    private final ArchiverPostUseCase categoryQueryService;

    @Operation(summary = "카테고리 목록 조회", description = "사용 가능한 카테고리 목록을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryListResponse>> getCategories(
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(categoryQueryService.getCategories()));
    }

    @Operation(summary = "카테고리별 장소 목록 조회",
            description = "categoryId를 가진 postPlace가 연결된 place 목록을 조회합니다. 최신 설명/조회수/저장수를 반환합니다.")
    @GetMapping("/{categoryId}/places")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getPlacesByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryPlaceListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                categoryQueryService.getPlacesByCategoryId(categoryId, oAuth2User.getUserId())));
    }
}
