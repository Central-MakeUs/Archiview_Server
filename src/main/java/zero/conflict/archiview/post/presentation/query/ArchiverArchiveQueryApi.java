package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.ArchiverArchivedPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

@Tag(name = "Archiver Place Query", description = "아카이버 아카이브 장소 조회 API")
public interface ArchiverArchiveQueryApi {

    @Operation(summary = "아카이브한 장소카드 목록 조회",
            description = "아카이버가 아카이브한 장소카드 목록을 필터 조건으로 조회합니다.")
    ResponseEntity<ApiResponse<ArchiverArchivedPostPlaceDto.ListResponse>> getMyArchivedPostPlaces(
            @Parameter(description = "지도 필터. ALL 또는 NEARBY", example = "ALL") EditorMapDto.MapFilter filter,
            @Parameter(description = "NEARBY 필터용 현재 위도", example = "37.5445") Double latitude,
            @Parameter(description = "NEARBY 필터용 현재 경도", example = "127.0560") Double longitude,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "아카이브한 장소 핀 지도 조회",
            description = "아카이버가 아카이브한 장소를 지도 핀 형태로 조회합니다.")
    ResponseEntity<ApiResponse<EditorMapDto.Response>> getMyArchivedMapPins(
            @Parameter(description = "지도 필터. ALL 또는 NEARBY", example = "ALL") EditorMapDto.MapFilter filter,
            @Parameter(description = "NEARBY 필터용 현재 위도", example = "37.5445") Double latitude,
            @Parameter(description = "NEARBY 필터용 현재 경도", example = "127.0560") Double longitude,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
