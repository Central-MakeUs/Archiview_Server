package zero.conflict.archiview.post.presentation.query.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;

@Tag(name = "Editor Place Query", description = "에디터용 장소 정보 관련 조회 API")
public interface EditorPostQueryApi {

    @Operation(summary = "에디터 인사이트 요약 조회",
            description = "기간 기준으로 에디터 인사이트 요약 지표를 조회합니다.")
    ResponseEntity<ApiResponse<EditorInsightDto.SummaryResponse>> getInsightSummary(
            @Parameter(description = "조회 기간. ALL, WEEK, MONTH 중 하나", example = "ALL") EditorInsightDto.Period period,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "에디터 인사이트 장소 목록 조회",
            description = "정렬 기준에 따라 인사이트 장소 목록을 조회합니다.")
    ResponseEntity<ApiResponse<EditorInsightDto.PlaceCardListResponse>> getInsightPlaces(
            @Parameter(description = "정렬 기준. RECENT, SAVE, VIEW, INSTAGRAM 중 하나", example = "RECENT") EditorInsightDto.PlaceSort sort,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "에디터 장소 상세 조회",
            description = "특정 placeId에 대한 에디터 관점 상세 정보를 조회합니다.")
    ResponseEntity<ApiResponse<EditorInsightDto.PlaceDetailResponse>> getInsightPlaceDetail(
            @Parameter(description = "조회할 place ID", example = "1") Long placeId,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user);

    @Operation(summary = "내 장소 지도 핀 조회",
            description = "필터와 선택 좌표 기준으로 에디터가 등록한 장소 핀 목록을 조회합니다.")
    ResponseEntity<ApiResponse<EditorMapDto.Response>> getMapPins(
            @Parameter(description = "지도 필터. ALL 또는 NEARBY", example = "ALL") EditorMapDto.MapFilter filter,
            @Parameter(description = "카테고리 ID 필터", example = "1") Long categoryId,
            @Parameter(description = "NEARBY 필터용 현재 위도", example = "37.5445") Double latitude,
            @Parameter(description = "NEARBY 필터용 현재 경도", example = "127.0560") Double longitude,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "내가 업로드한 장소 목록 조회",
            description = "필터/정렬 기준으로 에디터가 업로드한 장소 목록과 통계를 조회합니다.")
    ResponseEntity<ApiResponse<EditorUploadedPlaceDto.ListResponse>> getUploadedPlaces(
            @Parameter(description = "지도 필터. ALL 또는 NEARBY", example = "ALL") EditorMapDto.MapFilter filter,
            @Parameter(description = "정렬 기준", example = "UPDATED") EditorUploadedPlaceDto.PlaceSort sort,
            @Parameter(description = "카테고리 ID 필터", example = "1") Long categoryId,
            @Parameter(description = "NEARBY 필터용 현재 위도", example = "37.5445") Double latitude,
            @Parameter(description = "NEARBY 필터용 현재 경도", example = "127.0560") Double longitude,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "postPlaceId로 게시글 상세 조회",
            description = "특정 postPlace가 속한 게시글 상세와 게시글 내 모든 장소 정보를 조회합니다.")
    ResponseEntity<ApiResponse<EditorPostByPostPlaceDto.Response>> getPostByPostPlaceId(
            @Parameter(description = "조회 기준 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock);
}
