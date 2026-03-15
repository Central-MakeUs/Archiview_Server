package zero.conflict.archiview.user.presentation.command.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.dto.EditorProfileDto;

@Tag(name = "Editor User Command", description = "에디터용 프로필 업데이트 관련 API")
public interface EditorCommandApi {

    @Operation(summary = "에디터 내 프로필 수정",
            description = "로그인한 에디터 자신의 프로필 정보를 수정합니다. 닉네임, 소개, 인스타그램 ID, 이미지 등의 편집에 사용합니다.")
    ResponseEntity<ApiResponse<EditorProfileDto.Response>> updateMyProfile(
            @RequestBody(description = "에디터 프로필 수정 요청")
            @Valid EditorProfileDto.UpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
