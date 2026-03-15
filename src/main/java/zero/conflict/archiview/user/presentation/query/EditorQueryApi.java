package zero.conflict.archiview.user.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.dto.EditorProfileDto;

@Tag(name = "Editor User Query", description = "에디터 전용 조회 API")
public interface EditorQueryApi {

    @Operation(summary = "에디터 프로필 조회",
            description = "로그인한 에디터 자신의 프로필 정보를 조회합니다.")
    ResponseEntity<ApiResponse<EditorProfileDto.Response>> getMyProfile(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "에디터 인스타그램 ID 중복 확인",
            description = "에디터 프로필 등록/수정 전에 instagramId 중복 여부를 확인합니다.")
    ResponseEntity<ApiResponse<EditorProfileDto.InstagramIdCheckResponse>> checkInstagramId(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @NotBlank
            @Parameter(description = "중복 확인할 인스타그램 ID", example = "archiview_editor") String instagramId);

    @Operation(summary = "에디터 닉네임 중복 확인",
            description = "에디터 프로필 등록/수정 전에 닉네임 중복 여부를 확인합니다.")
    ResponseEntity<ApiResponse<EditorProfileDto.NicknameCheckResponse>> checkNickname(
            @Parameter(description = "mock 응답 사용 여부", example = "false") boolean useMock,
            @NotBlank
            @Parameter(description = "중복 확인할 닉네임", example = "감도높은에디터") String nickname);
}
