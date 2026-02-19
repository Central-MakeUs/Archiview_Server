package zero.conflict.archiview.post.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
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

@Tag(name = "Archiver Place Command", description = "아카이버용 장소 상호작용 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceCommandController {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Operation(summary = "인스타그램 유입 수 증가", description = "아카이버가 인스타그램 로고 클릭 시 해당 postPlace의 instagramInflowCount를 1 증가시킵니다.")
    @PostMapping("/post-places/{postPlaceId}/instagram-inflow")
    public ResponseEntity<ApiResponse<ArchiverPlaceCommandDto.InstagramInflowCountResponse>> increaseInstagramInflowCount(
            @PathVariable @Positive(message = "postPlaceId는 양수여야 합니다.") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long updatedCount = archiverPostUseCase.increasePostPlaceInstagramInflowCount(postPlaceId, oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                ArchiverPlaceCommandDto.InstagramInflowCountResponse.of(postPlaceId, updatedCount)));
    }

    @Operation(summary = "길찾기 수 증가", description = "아카이버가 길찾기 버튼 클릭 시 해당 postPlace의 directionCount를 1 증가시킵니다.")
    @PostMapping("/post-places/{postPlaceId}/direction-inflow")
    public ResponseEntity<ApiResponse<ArchiverPlaceCommandDto.DirectionCountResponse>> increaseDirectionCount(
            @PathVariable @Positive(message = "postPlaceId는 양수여야 합니다.") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long updatedCount = archiverPostUseCase.increasePostPlaceDirectionCount(postPlaceId, oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                ArchiverPlaceCommandDto.DirectionCountResponse.of(postPlaceId, updatedCount)));
    }
}
