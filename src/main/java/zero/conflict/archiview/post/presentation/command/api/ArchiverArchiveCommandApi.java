package zero.conflict.archiview.post.presentation.command.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;

@Tag(name = "Archiver Place Command", description = "아카이버 장소카드 아카이브 API")
public interface ArchiverArchiveCommandApi {

    @Operation(summary = "장소카드 아카이브",
            description = "아카이버가 특정 장소카드(postPlace)를 아카이브합니다.")
    ResponseEntity<ApiResponse<Void>> archivePostPlace(
            @Positive(message = "postPlaceId는 양수여야 합니다.")
            @Parameter(description = "아카이브할 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "장소카드 아카이브 해제",
            description = "아카이버가 아카이브한 장소카드(postPlace)를 해제합니다.")
    ResponseEntity<ApiResponse<Void>> unarchivePostPlace(
            @Parameter(description = "아카이브 해제할 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
