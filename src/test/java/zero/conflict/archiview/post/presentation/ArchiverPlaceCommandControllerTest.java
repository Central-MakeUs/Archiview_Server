package zero.conflict.archiview.post.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.editor.command.PostCommandService;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverPlaceCommandControllerTest extends ControllerTestSupport {

    @MockBean
    private PostCommandService postCommandService;

    @Test
    @DisplayName("인스타그램 유입 수 증가 성공")
    void increaseInstagramInflowCount_success() throws Exception {
        Long postPlaceId = 1L;
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        given(postCommandService.increasePostPlaceInstagramInflowCount(postPlaceId, actorId)).willReturn(57L);

        mockMvc.perform(post("/api/v1/archivers/post-places/{postPlaceId}/instagram-inflow", postPlaceId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postPlaceId").value(1L))
                .andExpect(jsonPath("$.data.instagramInflowCount").value(57L));
    }

    @Test
    @DisplayName("인스타그램 유입 수 증가 실패 - postPlace 없음")
    void increaseInstagramInflowCount_postPlaceNotFound() throws Exception {
        Long postPlaceId = 1L;
        UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        given(postCommandService.increasePostPlaceInstagramInflowCount(eq(postPlaceId), eq(actorId)))
                .willThrow(new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/archivers/post-places/{postPlaceId}/instagram-inflow", postPlaceId)
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_PLACE_NOT_FOUND"));
    }
}
