package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.query.EditorProfileQueryService;
import zero.conflict.archiview.user.dto.EditorProfileDto;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditorQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private EditorProfileQueryService editorProfileQueryService;

    @Test
    @DisplayName("아카이버용 에디터 화면 조회 - 성공")
    void getEditorArchiverView_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        EditorProfileDto.ArchiverViewResponse response = EditorProfileDto.ArchiverViewResponse.builder()
                .userId(editorId)
                .editorProfile(EditorProfileDto.Response.mock())
                .build();

        given(editorProfileQueryService.getEditorProfileForArchiver(editorId)).willReturn(response);

        mockMvc.perform(get("/api/v1/editors/{editorId}/archiver-view", editorId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(editorId.toString()))
                .andExpect(jsonPath("$.data.editorProfile.nickname").value("맛집탐방가"));
    }

    @Test
    @DisplayName("아카이버용 에디터 화면 조회 - mock")
    void getEditorArchiverView_mock() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000201");

        mockMvc.perform(get("/api/v1/editors/{editorId}/archiver-view", editorId)
                        .queryParam("useMock", "true")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.editorProfile").exists());
    }
}
