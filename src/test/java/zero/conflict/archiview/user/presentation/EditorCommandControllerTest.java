package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.command.EditorProfileCommandService;
import zero.conflict.archiview.user.dto.EditorProfileDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditorCommandControllerTest extends ControllerTestSupport {

    @MockBean
    private EditorProfileCommandService editorProfileCommandService;

    @Test
    @DisplayName("에디터 내 프로필 등록 - 성공")
    void createMyProfile_success() throws Exception {
        given(editorProfileCommandService.createProfile(
                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any()))
                .willReturn(EditorProfileDto.Response.mock());

        mockMvc.perform(post("/api/v1/editors/me/profile")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nickname", "맛집탐방가",
                                "instagramId", "editor_insta",
                                "instagramUrl", "https://www.instagram.com/editor_insta",
                                "introduction", "서울의 숨은 맛집을 기록합니다.",
                                "hashtags", new String[]{"#성수카페", "#디저트맛집"}
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("맛집탐방가"));
    }

    @Test
    @DisplayName("에디터 내 프로필 수정 - 성공")
    void updateMyProfile_success() throws Exception {
        given(editorProfileCommandService.updateProfile(
                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any()))
                .willReturn(EditorProfileDto.Response.mock());

        mockMvc.perform(put("/api/v1/editors/me/profile")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nickname", "새닉네임",
                                "instagramId", "new_insta",
                                "instagramUrl", "https://www.instagram.com/new_insta",
                                "introduction", "한줄 소개 수정",
                                "hashtags", new String[]{"#신규", "#태그"},
                                "profileImageUrl", "https://example.com/new.png"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.instagramId").value("editor_insta"));
    }
}
