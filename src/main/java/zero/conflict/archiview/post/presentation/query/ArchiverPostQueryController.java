package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;

@Tag(name = "Archiver Post Query", description = "아카이버용 장소 정보 관련 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverPostQueryController {

    @Operation(summary = "내 프로필 조회 (아카이버)", description = "로그인한 아카이버 자신의 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<zero.conflict.archiview.user.presentation.dto.ArchiverProfileDto.Response> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        // TODO: Service 연동
        return ResponseEntity
                .ok(zero.conflict.archiview.user.presentation.dto.ArchiverProfileDto.Response.builder().build());
    }

    @Operation(summary = "아카이버 공개 프로필 조회", description = "특정 아카이버의 공개된 프로필 정보를 조회합니다.")
    @GetMapping("/{archiverId}/profile")
    public ResponseEntity<zero.conflict.archiview.user.presentation.dto.ArchiverProfileDto.Response> getArchiverProfile(
            @PathVariable Long archiverId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return ResponseEntity
                .ok(zero.conflict.archiview.user.presentation.dto.ArchiverProfileDto.Response.builder().build());
    }
}
