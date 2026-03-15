package zero.conflict.archiview.user.presentation.command;

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
import zero.conflict.archiview.user.dto.UserDto;

@Tag(name = "User Command", description = "공통 프로필 업데이트 관련 API (CUD)")
public interface UserCommandApi {

    @Operation(summary = "에디터 프로필 등록",
            description = "로그인한 사용자의 에디터 프로필을 등록합니다. 성공 시 역할을 EDITOR로 전환하고 신규 토큰을 반환합니다.")
    ResponseEntity<ApiResponse<UserDto.RegisterEditorProfileResponse>> registerEditorProfile(
            @RequestBody(description = "에디터 프로필 생성 요청")
            @Valid EditorProfileDto.CreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "온보딩 완료",
            description = "최초 로그인 사용자가 에디터 또는 아카이버 역할을 선택해 온보딩을 완료합니다.")
    ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @RequestBody(description = "온보딩 완료 요청")
            @Valid UserDto.OnboardingRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "역할 전환",
            description = "뷰 전환용 API입니다. 요청한 역할 기준으로 토큰을 재발급합니다.")
    ResponseEntity<ApiResponse<UserDto.SwitchRoleResponse>> switchRole(
            @RequestBody(description = "전환 대상 역할 요청")
            @Valid UserDto.SwitchRoleRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "회원 탈퇴",
            description = "로그인한 사용자의 계정을 삭제합니다. 탈퇴 후 해당 계정 토큰은 더 이상 사용할 수 없습니다.")
    ResponseEntity<ApiResponse<Void>> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
