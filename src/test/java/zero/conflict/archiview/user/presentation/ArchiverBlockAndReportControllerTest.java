package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.archiver.command.PostReportCommandService;
import zero.conflict.archiview.user.application.archiver.command.EditorBlockCommandService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverBlockAndReportControllerTest extends ControllerTestSupport {

    @MockBean
    private PostReportCommandService postReportCommandService;

    @MockBean
    private EditorBlockCommandService editorBlockCommandService;

    @Test
    @DisplayName("postPlace 신고 성공")
    void reportPostPlace_success() throws Exception {
        doNothing().when(postReportCommandService).reportPostPlace(any(), eq(1L));

        mockMvc.perform(post("/api/v1/archivers/reports/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("postPlace 신고 취소 성공")
    void cancelReportPostPlace_success() throws Exception {
        doNothing().when(postReportCommandService).cancelReportPostPlace(any(), eq(1L));

        mockMvc.perform(delete("/api/v1/archivers/reports/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("에디터 차단 성공")
    void blockEditor_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        doNothing().when(editorBlockCommandService).blockEditor(any(), eq(editorId));

        mockMvc.perform(post("/api/v1/archivers/blocks/editors/{editorId}", editorId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("에디터 차단 해제 성공")
    void unblockEditor_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        doNothing().when(editorBlockCommandService).unblockEditor(any(), eq(editorId));

        mockMvc.perform(delete("/api/v1/archivers/blocks/editors/{editorId}", editorId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
