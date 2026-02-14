package zero.conflict.archiview.post.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.archiver.command.PostArchiveCommandService;
import zero.conflict.archiview.post.application.archiver.query.PostQueryService;
import zero.conflict.archiview.post.dto.ArchiverArchivedPostPlaceDto;
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
class ArchiverArchiveControllerTest extends ControllerTestSupport {

    @MockBean
    private PostArchiveCommandService postArchiveCommandService;

    @MockBean
    private PostQueryService postQueryService;

    @Test
    @DisplayName("postPlace 아카이브 성공")
    void archivePostPlace_success() throws Exception {
        doNothing().when(postArchiveCommandService).archivePostPlace(any(), eq(1L));

        mockMvc.perform(post("/api/v1/archivers/archives/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("postPlace 아카이브 해제 성공")
    void unarchivePostPlace_success() throws Exception {
        doNothing().when(postArchiveCommandService).unarchivePostPlace(any(), eq(1L));

        mockMvc.perform(delete("/api/v1/archivers/archives/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("내 아카이브 postPlace 목록 조회 성공")
    void getMyArchivedPostPlaces_success() throws Exception {
        ArchiverArchivedPostPlaceDto.ArchivedPostPlaceResponse item = ArchiverArchivedPostPlaceDto.ArchivedPostPlaceResponse.builder()
                .postPlaceId(11L)
                .placeId(101L)
                .placeName("성수 카페")
                .description("설명")
                .saveCount(10L)
                .viewCount(20L)
                .lastModifiedAt(LocalDateTime.of(2026, 2, 9, 12, 0, 0))
                .archivedAt(LocalDateTime.of(2026, 2, 10, 12, 0, 0))
                .build();
        ArchiverArchivedPostPlaceDto.ListResponse response = ArchiverArchivedPostPlaceDto.ListResponse.from(List.of(item));
        given(postQueryService.getMyArchivedPostPlaces(
                eq(EditorMapDto.MapFilter.ALL),
                isNull(),
                isNull(),
                any(UUID.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/archives/post-places")
                        .queryParam("filter", "ALL")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(11L))
                .andExpect(jsonPath("$.data.postPlaces[0].placeName").value("성수 카페"));
    }

    @Test
    @DisplayName("내 아카이브 장소 핀 지도 조회 성공")
    void getMyArchivedMapPins_success() throws Exception {
        EditorMapDto.PlacePinResponse pin = EditorMapDto.PlacePinResponse.builder()
                .placeId(1L)
                .name("한식당")
                .latitude(37.5445)
                .longitude(127.0560)
                .categories(List.of("한식", "양식"))
                .build();
        EditorMapDto.Response response = EditorMapDto.Response.from(List.of(pin));
        given(postQueryService.getMyArchivedMapPins(
                eq(EditorMapDto.MapFilter.ALL),
                isNull(),
                isNull(),
                any(UUID.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/archives/post-places/map/places")
                        .with(authenticatedUser())
                        .queryParam("filter", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pins[0].placeId").value(1L))
                .andExpect(jsonPath("$.data.pins[0].name").value("한식당"));
    }
}
