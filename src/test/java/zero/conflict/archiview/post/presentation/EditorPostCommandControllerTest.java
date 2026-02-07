package zero.conflict.archiview.post.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.command.PostCommandService;
import zero.conflict.archiview.post.dto.PostCommandDto;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditorPostCommandControllerTest extends ControllerTestSupport {

        @MockBean
        private PostCommandService postCommandService;

        @Test
        @DisplayName("게시글 생성 성공")
        void createPost_Success() throws Exception {
                // given
                PostCommandDto.Request.PlaceInfoRequest placeInfo = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("테스트 장소")
                                .description("테스트 설명")
                                .addressName("서울 노원구 공릉동 596-12")
                                .roadAddressName("인천 중구 백운로228번길 81-10")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 5분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("https://www.instagram.com/post")
                                .hashTags(java.util.List.of("#테스트", "#여행"))
                                .placeInfoRequestList(Collections.singletonList(placeInfo))
                                .build();

                PostCommandDto.Response.PlaceInfoResponse placeInfoResponse = PostCommandDto.Response.PlaceInfoResponse
                                .builder()
                                .placeId(101L)
                                .placeName("테스트 장소")
                                .addressName("서울 노원구 공릉동 596-12")
                                .roadAddressName("인천 중구 백운로228번길 81-10")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .build();

                PostCommandDto.Response mockResponse = PostCommandDto.Response.builder()
                                .postId(1L)
                                .url("https://www.instagram.com/post")
                                .hashTags(java.util.List.of("#테스트", "#여행"))
                                .placeInfoResponseList(Collections.singletonList(placeInfoResponse))
                                .build();

                given(postCommandService.createPost(any(PostCommandDto.Request.class),
                                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                                .willReturn(mockResponse);

                // when & then
                mockMvc.perform(post("/api/v1/editors/posts")
                                .with(authenticatedUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.postId").value(1))
                                .andExpect(jsonPath("$.data.url").value("https://www.instagram.com/post"))
                                .andExpect(jsonPath("$.data.hashTags[0]").value("#테스트"))
                                .andExpect(jsonPath("$.data.hashTags[1]").value("#여행"))
                                .andDo(print());
        }

        @Test
        @DisplayName("게시글 생성 실패 - URL 누락")
        void createPost_Fail_NoUrl() throws Exception {
                // given
                PostCommandDto.Request.PlaceInfoRequest placeInfo = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("테스트 장소")
                                .description("테스트 설명")
                                .addressName("주소1")
                                .roadAddressName("도로명주소1")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 4분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("") // URL 누락
                                .hashTags(java.util.List.of("#테스트", "#여행"))
                                .placeInfoRequestList(Collections.singletonList(placeInfo))
                                .build();

                // when & then
                mockMvc.perform(post("/api/v1/editors/posts")
                                .with(authenticatedUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andDo(print());
        }

        @Test
        @DisplayName("게시글 생성 실패 - 해시태그 누락")
        void createPost_Fail_NoHashTag() throws Exception {
                // given
                PostCommandDto.Request.PlaceInfoRequest placeInfo = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("테스트 장소")
                                .description("테스트 설명")
                                .addressName("주소1")
                                .roadAddressName("도로명주소1")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 4분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("https://www.instagram.com/post")
                                .hashTags(java.util.Collections.emptyList()) // 해시태그 누락
                                .placeInfoRequestList(Collections.singletonList(placeInfo))
                                .build();

                // when & then
                mockMvc.perform(post("/api/v1/editors/posts")
                                .with(authenticatedUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andDo(print());
        }

        @Test
        @DisplayName("게시글 생성 실패 - 장소 정보 누락")
        void createPost_Fail_NoPlaceInfo() throws Exception {
                // given
                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("https://www.instagram.com/post")
                                .hashTags(java.util.List.of("#테스트", "#여행"))
                                .placeInfoRequestList(Collections.emptyList()) // 장소 정보 누락
                                .build();

                // when & then
                mockMvc.perform(post("/api/v1/editors/posts")
                                .with(authenticatedUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andDo(print());
        }

        @Test
        @DisplayName("게시글 생성 실패 - 장소 설명 누락")
        void createPost_Fail_NoPlaceDescription() throws Exception {
                // given
                PostCommandDto.Request.PlaceInfoRequest placeInfo = PostCommandDto.Request.PlaceInfoRequest.builder()
                                .placeName("테스트 장소")
                                .description("") // 장소 설명 누락
                                .addressName("주소1")
                                .roadAddressName("도로명주소1")
                                .latitude(Double.valueOf("37.5665"))
                                .longitude(Double.valueOf("126.9780"))
                                .nearestStationWalkTime("도보 4분")
                                .build();

                PostCommandDto.Request request = PostCommandDto.Request.builder()
                                .url("https://www.instagram.com/post")
                                .hashTags(java.util.List.of("#테스트", "#여행"))
                                .placeInfoRequestList(Collections.singletonList(placeInfo))
                                .build();

                // when & then
                mockMvc.perform(post("/api/v1/editors/posts")
                                .with(authenticatedUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andDo(print());
        }
}
