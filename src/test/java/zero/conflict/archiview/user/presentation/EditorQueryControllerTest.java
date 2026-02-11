package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.editor.query.EditorProfileQueryService;
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
    @DisplayName("에디터 내 프로필 조회 - 성공")
    void getMyProfile_success() throws Exception {
        given(editorProfileQueryService.getMyProfile(org.mockito.ArgumentMatchers.any()))
                .willReturn(EditorProfileDto.Response.mock());

        mockMvc.perform(get("/api/v1/editors/me/profile")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("맛집탐방가"));
    }

    @Test
    @DisplayName("에디터 공개 프로필 조회 - 성공")
    void getEditorProfile_success() throws Exception {
        UUID editorId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        given(editorProfileQueryService.getEditorProfile(editorId)).willReturn(EditorProfileDto.Response.mock());

        mockMvc.perform(get("/api/v1/editors/{editorId}/profile", editorId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.instagramId").value("editor_insta"));
    }

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
    @DisplayName("에디터 인스타그램 ID 중복 확인 - 성공")
    void checkInstagramId_success() throws Exception {
        given(editorProfileQueryService.existsInstagramId("editor_insta")).willReturn(true);

        mockMvc.perform(get("/api/v1/editors/profile/instagram-id/exists")
                        .with(authenticatedUser())
                        .queryParam("instagramId", "editor_insta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exists").value(true));
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
