package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.archiver.query.ArchiverProfileQueryService;
import zero.conflict.archiview.user.application.archiver.query.TrustedEditorQueryService;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private ArchiverProfileQueryService archiverProfileQueryService;

    @MockBean
    private TrustedEditorQueryService trustedEditorQueryService;

    @Test
    @DisplayName("아카이버 내 프로필 조회 - 성공")
    void getMyProfile_success() throws Exception {
        ArchiverProfileDto.Response response = ArchiverProfileDto.Response.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .nickname("조용한 여우")
                .profileImageUrl("https://example.com/profile.png")
                .build();

        given(archiverProfileQueryService.getMyProfile(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/me/profile")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("조용한 여우"));
    }

    @Test
    @DisplayName("믿고 먹는 에디터 조회 - 성공")
    void getTrustedEditors_success() throws Exception {
        TrustedEditorDto.EditorResponse editor = TrustedEditorDto.EditorResponse.builder()
                .editorId(UUID.fromString("00000000-0000-0000-0000-000000000201"))
                .nickname("맛집탐방가")
                .instagramId("editor_insta")
                .build();
        TrustedEditorDto.ListResponse response = TrustedEditorDto.ListResponse.from(List.of(editor));

        given(trustedEditorQueryService.getTrustedEditors()).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/editors/trusted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.editors[0].nickname").value("맛집탐방가"));
    }

    @Test
    @DisplayName("믿고 먹는 에디터 조회 - mock")
    void getTrustedEditors_mock() throws Exception {
        mockMvc.perform(get("/api/v1/archivers/editors/trusted")
                        .queryParam("useMock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.editors").isArray());
    }
}
