package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.user.application.command.FollowCommandService;
import zero.conflict.archiview.user.application.query.EditorProfileQueryService;
import zero.conflict.archiview.user.application.query.FollowQueryService;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverFollowControllerTest extends ControllerTestSupport {

        private static final UUID ARCHIVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
        private static final UUID EDITOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

        @MockBean
        private FollowCommandService followCommandService;

        @MockBean
        private FollowQueryService followQueryService;

        @MockBean
        private EditorProfileQueryService editorProfileQueryService;

        @Test
        @DisplayName("팔로우 등록 성공")
        void follow_Success() throws Exception {
                doNothing().when(followCommandService).follow(eq(ARCHIVER_ID), eq(EDITOR_ID));

                FollowDto.CreateRequest request = FollowDto.CreateRequest.builder()
                                .editorId(EDITOR_ID)
                                .build();

                mockMvc.perform(post("/api/v1/archivers/follows")
                                .with(authenticatedArchiver())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());
        }

        @Test
        @DisplayName("팔로우 취소 성공")
        void unfollow_Success() throws Exception {
                doNothing().when(followCommandService).unfollow(eq(ARCHIVER_ID), eq(EDITOR_ID));

                mockMvc.perform(delete("/api/v1/archivers/follows/{editorId}", EDITOR_ID)
                                .with(authenticatedArchiver()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andDo(print());
        }

        @Test
        @DisplayName("내 팔로우 목록 조회 성공")
        void getMyFollowings_Success() throws Exception {
                FollowDto.FollowingResponse following = FollowDto.FollowingResponse.builder()
                                .editorId(EDITOR_ID)
                                .nickname("맛집탐방가")
                                .instagramId("editor_insta")
                                .instagramUrl("https://www.instagram.com/editor_insta")
                                .introduction("서울의 숨은 맛집을 기록합니다.")
                                .hashtags(List.of("#성수카페", "#디저트맛집"))
                                .profileImageUrl("https://picsum.photos/200/200?random=31")
                                .build();

                FollowDto.ListResponse response = FollowDto.ListResponse.from(List.of(following));
                given(followQueryService.getMyFollowings(org.mockito.ArgumentMatchers.any())).willReturn(response);

                mockMvc.perform(get("/api/v1/archivers/follows")
                                .with(authenticatedArchiver()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalCount").value(1))
                                .andExpect(jsonPath("$.data.editors[0].editorId").value(EDITOR_ID.toString()))
                                .andExpect(jsonPath("$.data.editors[0].nickname").value("맛집탐방가"))
                                .andDo(print());
        }

        @Test
        @DisplayName("내 팔로우 목록 조회 - mock 사용")
        void getMyFollowings_Mock() throws Exception {
                mockMvc.perform(get("/api/v1/archivers/follows")
                                .param("useMock", "true")
                                .with(authenticatedArchiver()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalCount").value(2))
                                .andExpect(jsonPath("$.data.editors[0].editorId").exists())
                                .andDo(print());
        }

        @Test
        @DisplayName("아카이버가 에디터 프로필 조회 성공")
        void getEditorProfile_Success() throws Exception {
                EditorProfileDto.Response response = EditorProfileDto.Response.builder()
                                .nickname("맛집탐방가")
                                .instagramId("editor_insta")
                                .instagramUrl("https://www.instagram.com/editor_insta")
                                .introduction("서울의 숨은 맛집을 기록합니다.")
                                .hashtags(List.of("#성수카페", "#디저트맛집"))
                                .profileImageUrl("https://picsum.photos/200/200?random=41")
                                .build();

                given(editorProfileQueryService.getEditorProfile(EDITOR_ID)).willReturn(response);

                mockMvc.perform(get("/api/v1/archivers/editors/{editorId}/profile", EDITOR_ID)
                                .with(authenticatedArchiver()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.nickname").value("맛집탐방가"))
                                .andExpect(jsonPath("$.data.instagramId").value("editor_insta"))
                                .andDo(print());
        }

        private RequestPostProcessor authenticatedArchiver() {
                return request -> {
                        User testUser = User.builder()
                                        .id(ARCHIVER_ID)
                                        .email("archiver@example.com")
                                        .name("archiver")
                                        .role(User.Role.ARCHIVER)
                                        .build();

                        CustomOAuth2User customOAuth2User = new CustomOAuth2User(
                                        testUser,
                                        Map.of("id", testUser.getId()));

                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                        customOAuth2User,
                                        "",
                                        customOAuth2User.getAuthorities());
                        context.setAuthentication(token);
                        SecurityContextHolder.setContext(context);
                        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);
                        return request;
                };
        }
}
