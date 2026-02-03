package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.query.CategoryQueryService;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category Query", description = "카테고리 조회 API")
public class CategoryQueryController {

    private final CategoryQueryService categoryQueryService;

    @Operation(summary = "카테고리 목록 조회", description = "사용 가능한 카테고리 목록을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryListResponse>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryQueryService.getCategories()));
    }
}
