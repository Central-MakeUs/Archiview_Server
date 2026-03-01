package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.editor.EditorUserUseCase;
import zero.conflict.archiview.user.dto.EditorProfileDto;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditorQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private EditorUserUseCase editorUserUseCase;

    @Test
    @DisplayName("에디터 내 프로필 조회 - 성공")
    void getMyProfile_success() throws Exception {
        given(editorUserUseCase.getMyProfile(org.mockito.ArgumentMatchers.any()))
                .willReturn(EditorProfileDto.Response.mock());

        mockMvc.perform(get("/api/v1/editors/me/profile")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("맛집탐방가"));
    }

    @Test
    @DisplayName("에디터 인스타그램 ID 중복 확인 - 성공")
    void checkInstagramId_success() throws Exception {
        given(editorUserUseCase.existsInstagramId("editor_insta")).willReturn(true);

        mockMvc.perform(get("/api/v1/editors/profile/instagram-id/exists")
                        .with(authenticatedUser())
                        .queryParam("instagramId", "editor_insta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("에디터 닉네임 중복 확인 - 성공")
    void checkNickname_success() throws Exception {
        given(editorUserUseCase.existsNickname("맛집탐방가")).willReturn(true);

        mockMvc.perform(get("/api/v1/editors/profile/nickname/exists")
                        .with(authenticatedUser())
                        .queryParam("nickname", "맛집탐방가"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exists").value(true));
    }
}
