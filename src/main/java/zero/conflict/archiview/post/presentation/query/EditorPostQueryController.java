package zero.conflict.archiview.post.presentation.query;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.global.infra.response.ApiResponse;
import zero.conflict.archiview.post.application.port.in.EditorPostUseCase;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto.PlaceSort;
import zero.conflict.archiview.post.dto.EditorMapDto.MapFilter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/editors")
@RequiredArgsConstructor
public class EditorPostQueryController implements EditorPostQueryApi {

    private final EditorPostUseCase editorPostUseCase;

    @Override
    @GetMapping("/me/insights/summary")
    public ResponseEntity<ApiResponse<EditorInsightDto.SummaryResponse>> getInsightSummary(
            @RequestParam(defaultValue = "ALL") EditorInsightDto.Period period,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User user) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorInsightDto.SummaryResponse.mock(period)));
        }
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.getInsightSummary(user.getUserId(), period)));
    }

    @Override
    @GetMapping("/me/insights/places")
    public ResponseEntity<ApiResponse<EditorInsightDto.PlaceCardListResponse>> getInsightPlaces(
            @RequestParam(defaultValue = "RECENT") EditorInsightDto.PlaceSort sort,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User user) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorInsightDto.PlaceCardListResponse.mock(sort)));
        }
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.getInsightPlaces(user.getUserId(), sort)));
    }

    @Override
    @GetMapping("/me/places/{placeId}")
    public ResponseEntity<ApiResponse<EditorInsightDto.PlaceDetailResponse>> getInsightPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User user) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorInsightDto.PlaceDetailResponse.mock(placeId)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                editorPostUseCase.getInsightPlaceDetail(user.getUserId(), placeId)));
    }

    @Override
    @GetMapping("/me/map/places")
    public ResponseEntity<ApiResponse<EditorMapDto.Response>> getMapPins(
            @RequestParam(defaultValue = "ALL") MapFilter filter,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorMapDto.Response.mock()));
        }

        return ResponseEntity.ok(ApiResponse.success(
                editorPostUseCase.getMapPins(
                        oAuth2User.getUserId(),
                        filter,
                        categoryId,
                        latitude,
                        longitude)));
    }

    @Override
    @GetMapping("/me/places")
    public ResponseEntity<ApiResponse<EditorUploadedPlaceDto.ListResponse>> getUploadedPlaces(
            @RequestParam(defaultValue = "ALL") MapFilter filter,
            @RequestParam(defaultValue = "UPDATED") EditorUploadedPlaceDto.PlaceSort sort,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "false") boolean useMock,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorUploadedPlaceDto.ListResponse.mock()));
        }
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.getUploadedPlaces(
                oAuth2User.getUserId(),
                filter,
                sort,
                categoryId,
                latitude,
                longitude)));
    }

    @Override
    @GetMapping("/me/posts/by-post-place/{postPlaceId}")
    public ResponseEntity<ApiResponse<EditorPostByPostPlaceDto.Response>> getPostByPostPlaceId(
            @PathVariable Long postPlaceId,
            @RequestParam(defaultValue = "false") boolean useMock) {
        if (useMock) {
            return ResponseEntity.ok(ApiResponse.success(EditorPostByPostPlaceDto.Response.mock(postPlaceId)));
        }
        return ResponseEntity.ok(ApiResponse.success(editorPostUseCase.getPostByPostPlaceId(postPlaceId)));
    }

}
