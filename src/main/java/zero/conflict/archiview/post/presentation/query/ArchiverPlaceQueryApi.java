package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.util.UUID;

@Tag(name = "Archiver Place Query", description = "아카이버용 장소 조회 API")
public interface ArchiverPlaceQueryApi {

    @Operation(summary = "요즘 핫한 장소 조회",
            description = "최근 업로드와 조회수를 반영한 핫한 장소 목록을 조회합니다.")
    ResponseEntity<ApiResponse<ArchiverHotPlaceDto.ListResponse>> getHotPlaces(
            @Parameter(description = "조회 개수", example = "10") int size,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "장소 상세 조회 (아카이버)",
            description = "placeId로 장소 상세와 연결된 게시글 목록을 조회합니다.")
    ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetail(
            @Parameter(description = "조회할 place ID", example = "1") Long placeId,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "특정 에디터가 업로드한 장소 상세 조회",
            description = "특정 에디터가 업로드한 장소카드만 반영한 장소 상세를 조회합니다.")
    ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetailByEditor(
            @Parameter(description = "에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(description = "조회할 place ID", example = "1") Long placeId,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "내 주변 1km 장소 조회",
            description = "현재 좌표 기준 반경 1km 내 장소 목록을 조회합니다.")
    ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getNearbyPlaces(
            @Parameter(description = "현재 위도", example = "37.5445") Double latitude,
            @Parameter(description = "현재 경도", example = "127.0560") Double longitude,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "에디터 업로드 장소 목록 조회 (아카이버)",
            description = "특정 에디터가 업로드한 장소카드 목록을 조회합니다.")
    ResponseEntity<ApiResponse<ArchiverEditorPostPlaceDto.ListResponse>> getEditorUploadedPostPlaces(
            @Parameter(description = "에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(description = "정렬 기준", example = "LATEST") ArchiverEditorPostPlaceDto.Sort sort,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "에디터 업로드 장소 핀 지도 조회 (아카이버)",
            description = "특정 에디터의 장소 핀을 필터 조건으로 조회합니다.")
    ResponseEntity<ApiResponse<EditorMapDto.Response>> getEditorMapPins(
            @Parameter(description = "에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(description = "지도 필터. ALL 또는 NEARBY", example = "ALL") EditorMapDto.MapFilter filter,
            @Parameter(description = "카테고리 ID 필터", example = "1") Long categoryId,
            @Parameter(description = "NEARBY 필터용 현재 위도", example = "37.5445") Double latitude,
            @Parameter(description = "NEARBY 필터용 현재 경도", example = "127.0560") Double longitude,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
