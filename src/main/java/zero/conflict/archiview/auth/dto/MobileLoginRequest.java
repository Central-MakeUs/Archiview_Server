package zero.conflict.archiview.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobileLoginRequest {

    @NotBlank(message = "idToken은 필수입니다.")
    private String idToken;

    @Pattern(
            regexp = "^(ARCHIVER|EDITOR|GUEST)?$",
            message = "role은 ARCHIVER, EDITOR, GUEST 중 하나여야 합니다.")
    private String role;
}
