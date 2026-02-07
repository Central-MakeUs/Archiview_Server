package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;

@Tag(name = "Archiver Place Query", description = "아카이버용 핫플레이스 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceQueryController {

    private final PostQueryService postQueryService;

    @Operation(summary = "요즘 핫한 장소 조회", description = "장소 기준 조회수 내림차순으로 핫플레이스를 조회합니다.")
    @GetMapping("/places/hot")
    public ResponseEntity<ApiResponse<ArchiverHotPlaceDto.ListResponse>> getHotPlaces(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverHotPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(postQueryService.getHotPlaces(size)));
    }

    @Operation(summary = "장소 상세 조회 (아카이버)", description = "placeId로 장소 상세와 연결된 게시글 목록을 조회합니다.")
    @GetMapping("/places/{placeId}")
    public ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverPlaceDetailDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(postQueryService.getArchiverPlaceDetail(placeId)));
    }

    @Operation(summary = "내 주변 1km 장소 조회", description = "현재 좌표 기준 1km 내 장소 목록을 조회합니다.")
    @GetMapping("/places/nearby")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getNearbyPlaces(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryPlaceListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(postQueryService.getNearbyPlacesWithin1km(latitude, longitude)));
    }
}
