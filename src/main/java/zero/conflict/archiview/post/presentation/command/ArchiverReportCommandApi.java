package zero.conflict.archiview.post.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;

@Tag(name = "Archiver Place Command", description = "아카이버 게시글 신고 API")
public interface ArchiverReportCommandApi {

    @Operation(summary = "장소카드 신고",
            description = "아카이버가 특정 장소카드를 신고하고 이후 조회에서 숨깁니다.")
    ResponseEntity<ApiResponse<Void>> reportPostPlace(
            @Positive(message = "postPlaceId는 양수여야 합니다.")
            @Parameter(description = "신고할 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "장소카드 신고 취소",
            description = "아카이버가 기존에 신고한 장소카드 신고를 취소합니다.")
    ResponseEntity<ApiResponse<Void>> cancelReportPostPlace(
            @Parameter(description = "신고 취소할 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
