package zero.conflict.archiview.post.presentation.query;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.ArchiverArchivedPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/archives/post-places")
public class ArchiverArchiveQueryController implements ArchiverArchiveQueryApi {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<ArchiverArchivedPostPlaceDto.ListResponse>> getMyArchivedPostPlaces(
            @RequestParam(defaultValue = "ALL") EditorMapDto.MapFilter filter,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverArchivedPostPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getMyArchivedPostPlaces(
                        filter,
                        latitude,
                        longitude,
                        oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/map/places")
    public ResponseEntity<ApiResponse<EditorMapDto.Response>> getMyArchivedMapPins(
            @RequestParam(defaultValue = "ALL") EditorMapDto.MapFilter filter,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorMapDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getMyArchivedMapPins(
                        filter,
                        latitude,
                        longitude,
                        oAuth2User.getUserId())));
    }
}
