package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.editor.command.UserCommandService;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.UserDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserCommandControllerTest extends ControllerTestSupport {

    @MockBean
    private UserCommandService userCommandService;

    @Test
    @DisplayName("에디터 프로필 등록 - 성공")
    void registerEditorProfile_success() throws Exception {
        UserDto.RegisterEditorProfileResponse response = UserDto.RegisterEditorProfileResponse.builder()
                .accessToken("editor-access")
                .role(User.Role.EDITOR)
                .editorProfile(EditorProfileDto.Response.mock())
                .build();

        given(userCommandService.registerEditorProfile(
                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/users/me/editor-profile")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nickname", "맛집탐방가",
                                "instagramId", "editor_insta",
                                "instagramUrl", "https://www.instagram.com/editor_insta",
                                "introduction", "서울의 숨은 맛집을 기록합니다.",
                                "hashtags", new String[]{"#성수카페", "#디저트맛집"},
                                "profileImageUrl", "https://example.com/profile.png"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("editor-access"))
                .andExpect(jsonPath("$.data.role").value("EDITOR"))
                .andExpect(jsonPath("$.data.editorProfile.nickname").value("맛집탐방가"));
    }

    @Test
    @DisplayName("온보딩 완료 - 성공")
    void completeOnboarding_success() throws Exception {
        doNothing().when(userCommandService)
                .completeOnboarding(eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any());

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "EDITOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("온보딩 완료 - 잘못된 role 문자열은 USER_014를 반환한다")
    void completeOnboarding_invalidRoleString_returnsInvalidRoleError() throws Exception {
        mockMvc.perform(post("/api/v1/users/onboarding")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "INVALID_ROLE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_014"));
    }

    @Test
    @DisplayName("온보딩 완료 - GUEST role은 USER_014를 반환한다")
    void completeOnboarding_guestRole_returnsInvalidRoleError() throws Exception {
        willThrow(new DomainException(UserErrorCode.INVALID_ROLE_SWITCH_TARGET))
                .given(userCommandService)
                .completeOnboarding(eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any());

        mockMvc.perform(post("/api/v1/users/onboarding")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "GUEST"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_014"));
    }

    @Test
    @DisplayName("역할 전환 - 성공")
    void switchRole_success() throws Exception {
        UserDto.SwitchRoleResponse response = UserDto.SwitchRoleResponse.builder()
                .accessToken("new-access")
                .role(User.Role.ARCHIVER)
                .build();

        given(userCommandService.switchRole(
                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/users/switch-role")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ARCHIVER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.role").value("ARCHIVER"));
    }

    @Test
    @DisplayName("역할 전환 - 잘못된 role 문자열은 USER_014를 반환한다")
    void switchRole_invalidRoleString_returnsInvalidRoleError() throws Exception {
        mockMvc.perform(post("/api/v1/users/switch-role")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "INVALID_ROLE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_014"));
    }

    @Test
    @DisplayName("역할 전환 - GUEST role은 USER_014를 반환한다")
    void switchRole_guestRole_returnsInvalidRoleError() throws Exception {
        willThrow(new DomainException(UserErrorCode.INVALID_ROLE_SWITCH_TARGET))
                .given(userCommandService)
                .switchRole(eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")), any());

        mockMvc.perform(post("/api/v1/users/switch-role")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "GUEST"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_014"));
    }

}
