package zero.conflict.archiview.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppleMobileLoginRequest {

    @NotBlank(message = "idToken은 필수입니다.")
    private String idToken;

    @NotBlank(message = "authorizationCode는 필수입니다.")
    private String authorizationCode;
}
