package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.command.UserCommandService;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.dto.UserDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserCommandControllerTest extends ControllerTestSupport {

    @MockBean
    private UserCommandService userCommandService;

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
}
