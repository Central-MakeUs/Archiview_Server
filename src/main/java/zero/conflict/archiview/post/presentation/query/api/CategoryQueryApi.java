package zero.conflict.archiview.post.presentation.query.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

@Tag(name = "Category Query", description = "카테고리 조회 API")
public interface CategoryQueryApi {

    @Operation(summary = "카테고리 목록 조회",
            description = "사용 가능한 전체 카테고리 목록을 조회합니다.")
    ResponseEntity<ApiResponse<CategoryQueryDto.CategoryListResponse>> getCategories(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock);

    @Operation(summary = "카테고리별 장소 목록 조회",
            description = "특정 categoryId에 연결된 place 목록과 최신 설명/지표를 조회합니다.")
    ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getPlacesByCategoryId(
            @Parameter(description = "카테고리 ID", example = "1") Long categoryId,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
