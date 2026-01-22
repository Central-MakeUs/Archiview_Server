package zero.conflict.archiview.user.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.presentation.dto.EditorProfileDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/editors")
@Tag(name = "Editor Profile Command", description = "에디터용 프로필 업데이트 관련 API")
public class EditorCommandController {

    @Operation(summary = "에디터 내 프로필 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @RequestBody @Valid EditorProfileDto.UpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        // TODO: UserCommandService 연동
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
