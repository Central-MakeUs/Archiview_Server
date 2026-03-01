package zero.conflict.archiview.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoMobileLoginRequest {

    @NotBlank(message = "accessToken은 필수입니다.")
    private String accessToken;
}
