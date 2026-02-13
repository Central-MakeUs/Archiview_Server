package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.archiver.command.PostReportCommandService;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.user.application.archiver.command.EditorBlockCommandService;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.BDDMockito.willThrow;
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
    @DisplayName("내 장소카드 신고는 예외")
    void reportPostPlace_self_throwsException() throws Exception {
        willThrow(new DomainException(PostErrorCode.SELF_REPORT_NOT_ALLOWED))
                .given(postReportCommandService).reportPostPlace(any(), eq(1L));

        mockMvc.perform(post("/api/v1/archivers/reports/post-places/{postPlaceId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_SELF_REPORT_NOT_ALLOWED"));
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
    @DisplayName("자기 자신 차단은 예외")
    void blockEditor_self_throwsException() throws Exception {
        UUID selfId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        willThrow(new DomainException(UserErrorCode.SELF_BLOCK_NOT_ALLOWED))
                .given(editorBlockCommandService).blockEditor(any(), eq(selfId));

        mockMvc.perform(post("/api/v1/archivers/blocks/editors/{editorId}", selfId)
                        .with(authenticatedUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_018"));
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
