package zero.conflict.archiview.user.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.port.in.EditorUserUseCase;
import zero.conflict.archiview.user.dto.UserDto;

@Tag(name = "User Command", description = "공통 프로필 업데이트 관련 API (CUD)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserCommandController {

    private final EditorUserUseCase userCommandService;

    @Operation(summary = "온보딩 완료", description = "최초 로그인 후 에디터 또는 아카이버 역할을 선택하여 회원가입을 완료합니다.")
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @RequestBody @Valid UserDto.OnboardingRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        userCommandService.completeOnboarding(oAuth2User.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "역할 전환", description = "아카이버/에디터 역할을 전환하고 해당 역할용 토큰을 발급합니다.")
    @PostMapping("/switch-role")
    public ResponseEntity<ApiResponse<UserDto.SwitchRoleResponse>> switchRole(
            @RequestBody @Valid UserDto.SwitchRoleRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity.ok(ApiResponse.success(userCommandService.switchRole(oAuth2User.getUserId(), request)));
    }
}
