package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.ArchiverEditorPostPlaceDto;
import zero.conflict.archiview.post.dto.CategoryQueryDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverPlaceQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private PostQueryService postQueryService;

    @Test
    @DisplayName("내 주변 1km 장소 조회 - 성공")
    void getNearbyPlaces_success() throws Exception {
        CategoryQueryDto.CategoryPlaceResponse place = CategoryQueryDto.CategoryPlaceResponse.builder()
                .placeId(1L)
                .placeName("성수 감성 카페")
                .latestDescription("최근 설명")
                .viewCount(200L)
                .saveCount(45L)
                .build();
        CategoryQueryDto.CategoryPlaceListResponse response = CategoryQueryDto.CategoryPlaceListResponse.builder()
                .totalCount(1L)
                .places(List.of(place))
                .build();
        given(postQueryService.getNearbyPlacesWithin1km(37.5445, 127.0560)).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/places/nearby")
                        .queryParam("latitude", "37.5445")
                .queryParam("longitude", "127.0560"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.places[0].placeName").value("성수 감성 카페"))
                .andExpect(jsonPath("$.data.places[0].saveCount").value(45));
    }

    @Test
    @DisplayName("내 주변 1km 장소 조회 - mock")
    void getNearbyPlaces_mock() throws Exception {
        mockMvc.perform(get("/api/v1/archivers/places/nearby")
                        .queryParam("latitude", "37.5445")
                        .queryParam("longitude", "127.0560")
                .queryParam("useMock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.places").isArray());
    }

    @Test
    @DisplayName("에디터 업로드 장소 목록 조회 - 성공")
    void getEditorUploadedPostPlaces_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000111");
        ArchiverEditorPostPlaceDto.PostPlaceResponse item = ArchiverEditorPostPlaceDto.PostPlaceResponse.builder()
                .postPlaceId(11L)
                .placeName("성수 감성 카페")
                .description("설명")
                .saveCount(20L)
                .viewCount(100L)
                .imageUrl("https://img.url")
                .build();
        ArchiverEditorPostPlaceDto.ListResponse response = ArchiverEditorPostPlaceDto.ListResponse.builder()
                .totalCount(1L)
                .postPlaces(List.of(item))
                .build();

        given(postQueryService.getEditorUploadedPostPlaces(editorId, ArchiverEditorPostPlaceDto.Sort.LATEST))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/editors/{userId}/post-places", editorId)
                        .with(authenticatedUser())
                        .queryParam("sort", "LATEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(11L))
                .andExpect(jsonPath("$.data.postPlaces[0].placeName").value("성수 감성 카페"))
                .andExpect(jsonPath("$.data.postPlaces[0].saveCount").value(20L))
                .andExpect(jsonPath("$.data.postPlaces[0].viewCount").value(100L));
    }

    @Test
    @DisplayName("에디터 업로드 장소 목록 조회 - mock")
    void getEditorUploadedPostPlaces_mock() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000111");

        mockMvc.perform(get("/api/v1/archivers/editors/{userId}/post-places", editorId)
                        .with(authenticatedUser())
                        .queryParam("useMock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.postPlaces").isArray());
    }

    @Test
    @DisplayName("에디터 업로드 장소 핀 지도 조회 - 성공(ALL)")
    void getEditorMapPins_all_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000121");
        EditorMapDto.PlacePinResponse pin = EditorMapDto.PlacePinResponse.builder()
                .placeId(1L)
                .name("한식당")
                .latitude(37.5445)
                .longitude(127.0560)
                .categories(List.of("한식", "양식"))
                .build();
        EditorMapDto.Response response = EditorMapDto.Response.from(List.of(pin));

        given(postQueryService.getMapPinsForArchiver(
                editorId,
                EditorMapDto.MapFilter.ALL,
                List.of(1L, 2L),
                null,
                null)).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/editors/{editorId}/map/places", editorId)
                        .with(authenticatedUser())
                        .queryParam("filter", "ALL")
                        .queryParam("categoryIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pins[0].placeId").value(1L))
                .andExpect(jsonPath("$.data.pins[0].name").value("한식당"));
    }

    @Test
    @DisplayName("에디터 업로드 장소 핀 지도 조회 - 성공(NEARBY)")
    void getEditorMapPins_nearby_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000122");
        given(postQueryService.getMapPinsForArchiver(
                editorId,
                EditorMapDto.MapFilter.NEARBY,
                null,
                37.5445,
                127.0560)).willReturn(EditorMapDto.Response.empty());

        mockMvc.perform(get("/api/v1/archivers/editors/{editorId}/map/places", editorId)
                        .with(authenticatedUser())
                        .queryParam("filter", "NEARBY")
                        .queryParam("latitude", "37.5445")
                        .queryParam("longitude", "127.0560"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pins").isArray());
    }

    @Test
    @DisplayName("에디터 업로드 장소 핀 지도 조회 - mock")
    void getEditorMapPins_mock() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000123");

        mockMvc.perform(get("/api/v1/archivers/editors/{editorId}/map/places", editorId)
                        .with(authenticatedUser())
                        .queryParam("useMock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pins").isArray());
    }
}
