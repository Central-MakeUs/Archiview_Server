package zero.conflict.archiview.post.presentation.command.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.ArchiverPlaceCommandDto;

@Tag(name = "Archiver Place Command", description = "아카이버용 장소 상호작용 API")
public interface ArchiverPlaceCommandApi {

    @Operation(summary = "인스타그램 유입 수 증가",
            description = "아카이버가 인스타그램 로고를 클릭할 때 해당 postPlace의 instagramInflowCount를 1 증가시킵니다.")
    ResponseEntity<ApiResponse<ArchiverPlaceCommandDto.InstagramInflowCountResponse>> increaseInstagramInflowCount(
            @Positive(message = "postPlaceId는 양수여야 합니다.")
            @Parameter(description = "유입 수를 증가시킬 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);

    @Operation(summary = "길찾기 수 증가",
            description = "아카이버가 길찾기 버튼을 클릭할 때 해당 postPlace의 directionCount를 1 증가시킵니다.")
    ResponseEntity<ApiResponse<ArchiverPlaceCommandDto.DirectionCountResponse>> increaseDirectionCount(
            @Positive(message = "postPlaceId는 양수여야 합니다.")
            @Parameter(description = "길찾기 수를 증가시킬 postPlace ID", example = "101") Long postPlaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User);
}
