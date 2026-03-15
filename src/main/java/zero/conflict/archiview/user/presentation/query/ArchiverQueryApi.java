package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.BlockDto;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

import java.util.UUID;

@Tag(name = "Archiver User Query", description = "아카이버 전용 조회 API")
public interface ArchiverQueryApi {

    @Operation(summary = "내 프로필 조회 (아카이버)",
            description = "로그인한 아카이버 자신의 프로필 정보를 조회합니다.")
    ResponseEntity<ApiResponse<ArchiverProfileDto.Response>> getMyProfile(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "내 팔로우 목록",
            description = "아카이버가 팔로우 중인 에디터 목록을 조회합니다.")
    ResponseEntity<ApiResponse<FollowDto.ListResponse>> getMyFollowings(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "내 차단 에디터 목록",
            description = "아카이버가 차단한 에디터 목록을 조회합니다.")
    ResponseEntity<ApiResponse<BlockDto.ListResponse>> getMyBlockedEditors(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "믿고 먹는 에디터 조회",
            description = "팔로워 수와 등록한 포스트 장소 수를 기준으로 추천 에디터 목록을 조회합니다.")
    ResponseEntity<ApiResponse<TrustedEditorDto.ListResponse>> getTrustedEditors(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock);

    @Operation(summary = "에디터 프로필 조회 (아카이버)",
            description = "아카이버가 특정 에디터의 공개 프로필 정보를 조회합니다.")
    ResponseEntity<ApiResponse<EditorProfileDto.ArchiverEditorProfileResponse>> getEditorProfile(
            @Parameter(description = "조회할 에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
