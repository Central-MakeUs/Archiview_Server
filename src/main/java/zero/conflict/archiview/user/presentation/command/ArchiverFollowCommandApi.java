package zero.conflict.archiview.user.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;

import java.util.UUID;

@Tag(name = "Archiver User Command", description = "아카이버 팔로우 API")
public interface ArchiverFollowCommandApi {

    @Operation(summary = "팔로우 등록",
            description = "아카이버가 특정 에디터를 팔로우합니다.")
    ResponseEntity<ApiResponse<Void>> follow(
            @Parameter(description = "팔로우할 에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "팔로우 취소",
            description = "아카이버가 특정 에디터 팔로우를 취소합니다.")
    ResponseEntity<ApiResponse<Void>> unfollow(
            @Parameter(description = "팔로우 취소할 에디터 userId", example = "00000000-0000-0000-0000-000000000101") UUID editorId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
