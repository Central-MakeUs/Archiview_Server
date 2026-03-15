package zero.conflict.archiview.user.presentation.command.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.user.application.port.in.ArchiverUserUseCase;
import zero.conflict.archiview.user.presentation.command.api.ArchiverFollowCommandApi;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/follows")
public class ArchiverFollowCommandController implements ArchiverFollowCommandApi {

    private final ArchiverUserUseCase archiverUserUseCase;

    @Override
    @PostMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @PathVariable UUID editorId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.follow(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Override
    @DeleteMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable UUID editorId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.unfollow(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
