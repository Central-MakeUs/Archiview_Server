package zero.conflict.archiview.post.presentation.command;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.ArchiverPlaceCommandDto;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceCommandController implements ArchiverPlaceCommandApi {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Override
    @PostMapping("/post-places/{postPlaceId}/instagram-inflow")
    public ResponseEntity<ApiResponse<ArchiverPlaceCommandDto.InstagramInflowCountResponse>> increaseInstagramInflowCount(
            @PathVariable Long postPlaceId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long updatedCount = archiverPostUseCase.increasePostPlaceInstagramInflowCount(postPlaceId, oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                ArchiverPlaceCommandDto.InstagramInflowCountResponse.of(postPlaceId, updatedCount)));
    }

    @Override
    @PostMapping("/post-places/{postPlaceId}/direction-inflow")
    public ResponseEntity<ApiResponse<ArchiverPlaceCommandDto.DirectionCountResponse>> increaseDirectionCount(
            @PathVariable Long postPlaceId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long updatedCount = archiverPostUseCase.increasePostPlaceDirectionCount(postPlaceId, oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                ArchiverPlaceCommandDto.DirectionCountResponse.of(postPlaceId, updatedCount)));
    }
}
