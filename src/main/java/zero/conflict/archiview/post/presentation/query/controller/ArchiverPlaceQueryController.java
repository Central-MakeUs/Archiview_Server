package zero.conflict.archiview.post.presentation.query.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.application.port.in.ArchiverPostUseCase;
import zero.conflict.archiview.post.dto.ArchiverHotPlaceDto;
import zero.conflict.archiview.post.dto.ArchiverPlaceDetailDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.presentation.query.api.ArchiverPlaceQueryApi;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceQueryController implements ArchiverPlaceQueryApi {

    private final ArchiverPostUseCase archiverPostUseCase;

    @Override
    @GetMapping("/places/hot")
    public ResponseEntity<ApiResponse<ArchiverHotPlaceDto.ListResponse>> getHotPlaces(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverHotPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getHotPlaces(size, oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/places/{placeId}")
    public ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverPlaceDetailDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getArchiverPlaceDetail(placeId, oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/editors/{editorId}/places/{placeId}")
    public ResponseEntity<ApiResponse<ArchiverPlaceDetailDto.Response>> getPlaceDetailByEditor(
            @PathVariable UUID editorId,
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverPlaceDetailDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getArchiverPlaceDetailByEditor(placeId, editorId, oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/places/nearby")
    public ResponseEntity<ApiResponse<CategoryQueryDto.CategoryPlaceListResponse>> getNearbyPlaces(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(CategoryQueryDto.CategoryPlaceListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getNearbyPlacesWithin1km(
                latitude,
                longitude,
                oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/editors/{editorId}/post-places")
    public ResponseEntity<ApiResponse<ArchiverEditorPostPlaceDto.ListResponse>> getEditorUploadedPostPlaces(
            @PathVariable UUID editorId,
            @RequestParam(defaultValue = "LATEST") ArchiverEditorPostPlaceDto.Sort sort,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(ArchiverEditorPostPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(archiverPostUseCase.getEditorUploadedPostPlaces(
                editorId,
                sort,
                oAuth2User.getUserId())));
    }

    @Override
    @GetMapping("/editors/{editorId}/map/places")
    public ResponseEntity<ApiResponse<EditorMapDto.Response>> getEditorMapPins(
            @PathVariable UUID editorId,
            @RequestParam(defaultValue = "ALL") EditorMapDto.MapFilter filter,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorMapDto.Response.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                archiverPostUseCase.getMapPinsForArchiver(
                        editorId,
                        filter,
                        categoryId,
                        latitude,
                        longitude,
                        oAuth2User.getUserId())));
    }
}
