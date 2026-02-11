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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
}
