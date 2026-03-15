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
import zero.conflict.archiview.user.presentation.command.api.ArchiverBlockCommandApi;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/blocks/editors")
public class ArchiverBlockCommandController implements ArchiverBlockCommandApi {

    private final ArchiverUserUseCase archiverUserUseCase;

    @Override
    @PostMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> blockEditor(
            @PathVariable UUID editorId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.blockEditor(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Override
    @DeleteMapping("/{editorId}")
    public ResponseEntity<ApiResponse<Void>> unblockEditor(
            @PathVariable UUID editorId,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        archiverUserUseCase.unblockEditor(oAuth2User.getUserId(), editorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
