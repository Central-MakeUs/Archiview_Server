package zero.conflict.archiview.user.presentation.command.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;

import java.util.UUID;

@Tag(name = "Archiver User Command", description = "아카이버 사용자 차단 API")
public interface ArchiverBlockCommandApi {

    @Operation(summary = "에디터 차단",
            description = "아카이버가 특정 에디터를 차단합니다. 차단 후 해당 에디터의 게시글/장소카드가 조회에서 제외됩니다.")
    ResponseEntity<ApiResponse<Void>> blockEditor(
            @Parameter(description = "차단할 에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "에디터 차단 해제",
            description = "아카이버가 특정 에디터 차단을 해제합니다.")
    ResponseEntity<ApiResponse<Void>> unblockEditor(
            @Parameter(description = "차단 해제할 에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
