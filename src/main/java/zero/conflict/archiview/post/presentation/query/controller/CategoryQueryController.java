package zero.conflict.archiview.post.presentation.query.controller;

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
import zero.conflict.archiview.post.presentation.query.api.CategoryQueryApi;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryQueryController implements CategoryQueryApi {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Override
    @GetMapping("")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryListResponse>> getCategories(
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getCategories()));
    }

    @Override
    @GetMapping("/{categoryId}/places")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getPlacesByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryPlaceListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getPlacesByCategoryId(categoryId, oAuth2User.getUserId())));
    }
}
