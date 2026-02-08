package zero.conflict.archiview.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.user.domain.User;

public class UserDto {

    @Getter
    @NoArgsConstructor
    public static class OnboardingRequest {
        @NotNull(message = "역할 선택은 필수입니다.")
        @Schema(description = "선택할 역할 (ARCHIVER 또는 EDITOR)", example = "EDITOR")
        private User.Role role;
    }

    @Getter
    @NoArgsConstructor
    public static class SwitchRoleRequest {
        @NotNull(message = "전환할 역할 선택은 필수입니다.")
        @Schema(description = "전환할 역할 (ARCHIVER 또는 EDITOR)", example = "ARCHIVER")
        private User.Role role;
    }

    @Getter
    @Builder
    public static class SwitchRoleResponse {
        private String accessToken;
        private User.Role role;
    }
}
