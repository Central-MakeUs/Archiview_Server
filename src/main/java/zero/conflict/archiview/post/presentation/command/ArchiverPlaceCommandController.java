package zero.conflict.archiview.post.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.command.PostCommandService;

@Tag(name = "Archiver Place Command", description = "아카이버용 장소 상호작용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceCommandController {

    private final PostCommandService postCommandService;

    @Operation(summary = "장소 조회수 증가", description = "아카이버가 장소 상세를 조회할 때 조회수를 증가시킵니다.")
    @PostMapping("/post-places/{postPlaceId}/view")
    public ResponseEntity<ApiResponse<Void>> increasePostPlaceViewCount(
            @PathVariable Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        postCommandService.increasePostPlaceViewCount(postPlaceId, oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
