package zero.conflict.archiview.post.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.archiver.command.PostSaveCommandService;
import zero.conflict.archiview.post.application.archiver.query.PostQueryService;
import zero.conflict.archiview.post.dto.ArchiverSavedPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorMapDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverSaveControllerTest extends ControllerTestSupport {

    @MockBean
    private PostSaveCommandService postSaveCommandService;

    @MockBean
    private PostQueryService postQueryService;

    @Test
    @DisplayName("postPlace 저장 성공")
    void savePostPlace_success() throws Exception {
        doNothing().when(postSaveCommandService).savePostPlace(any(), eq(1L));

        mockMvc.perform(post("/api/v1/archivers/saves/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("postPlace 저장 해제 성공")
    void unsavePostPlace_success() throws Exception {
        doNothing().when(postSaveCommandService).unsavePostPlace(any(), eq(1L));

        mockMvc.perform(delete("/api/v1/archivers/saves/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("내 저장 postPlace 목록 조회 성공")
    void getMySavedPostPlaces_success() throws Exception {
        ArchiverSavedPostPlaceDto.SavedPostPlaceResponse item = ArchiverSavedPostPlaceDto.SavedPostPlaceResponse.builder()
                .postPlaceId(11L)
                .placeId(101L)
                .placeName("성수 카페")
                .description("설명")
                .saveCount(10L)
                .viewCount(20L)
                .lastModifiedAt(LocalDateTime.of(2026, 2, 9, 12, 0, 0))
                .savedAt(LocalDateTime.of(2026, 2, 10, 12, 0, 0))
                .build();
        ArchiverSavedPostPlaceDto.ListResponse response = ArchiverSavedPostPlaceDto.ListResponse.from(List.of(item));
        given(postQueryService.getMySavedPostPlaces(any(UUID.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/saves/post-places")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(11L))
                .andExpect(jsonPath("$.data.postPlaces[0].placeName").value("성수 카페"));
    }

    @Test
    @DisplayName("내 저장 장소 핀 지도 조회 성공")
    void getMySavedMapPins_success() throws Exception {
        EditorMapDto.PlacePinResponse pin = EditorMapDto.PlacePinResponse.builder()
                .placeId(1L)
                .name("한식당")
                .latitude(37.5445)
                .longitude(127.0560)
                .categories(List.of("한식", "양식"))
                .build();
        EditorMapDto.Response response = EditorMapDto.Response.from(List.of(pin));
        given(postQueryService.getMySavedMapPins(
                eq(EditorMapDto.MapFilter.ALL),
                eq(List.of(1L, 2L)),
                isNull(),
                isNull(),
                any(UUID.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/saves/post-places/map/places")
                        .with(authenticatedUser())
                        .queryParam("filter", "ALL")
                        .queryParam("categoryIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pins[0].placeId").value(1L))
                .andExpect(jsonPath("$.data.pins[0].name").value("한식당"));
    }
}
