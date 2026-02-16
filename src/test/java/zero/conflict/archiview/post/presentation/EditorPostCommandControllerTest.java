package zero.conflict.archiview.post.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.editor.command.PostCommandService;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.global.error.DomainException;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        PostCommandDto.CreateRequest.CreatePlaceInfoRequest placeInfo = PostCommandDto.CreateRequest.CreatePlaceInfoRequest.builder()
                .placeName("테스트 장소")
                .description("테스트 설명")
                .addressName("서울 노원구 공릉동 596-12")
                .roadAddressName("인천 중구 백운로228번길 81-10")
                .latitude(Double.valueOf("37.5665"))
                .longitude(Double.valueOf("126.9780"))
                .nearestStationWalkTime("도보 5분")
                .phoneNumber("02-1234-5678")
                .build();

        PostCommandDto.CreateRequest request = PostCommandDto.CreateRequest.builder()
                .url("https://www.instagram.com/post")
                .hashTags(java.util.List.of("#테스트", "#여행"))
                .placeInfoRequestList(Collections.singletonList(placeInfo))
                .build();

        PostCommandDto.Response.PlaceInfoResponse placeResponse = PostCommandDto.Response.PlaceInfoResponse.builder()
                .placeId(11L)
                .placeName("테스트 장소")
                .name("테스트 장소")
                .phoneNumber("02-1234-5678")
                .build();

        PostCommandDto.Response mockResponse = PostCommandDto.Response.builder()
                .postId(1L)
                .url("https://www.instagram.com/post")
                .hashTags(java.util.List.of("#테스트", "#여행"))
                .placeInfoResponseList(Collections.singletonList(placeResponse))
                .build();

        given(postCommandService.createPost(any(PostCommandDto.CreateRequest.class),
                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/editors/posts")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(1))
                .andExpect(jsonPath("$.data.placeInfoResponseList[0].phoneNumber").value("02-1234-5678"));
    }

    @Test
    @DisplayName("게시글 이미지 presigned URL 발급 성공")
    void createPostImagePresignedUrl_success() throws Exception {
        PresignedUrlCommandDto.Request request = PresignedUrlCommandDto.Request.builder()
                .filename("photo.png")
                .contentType("image/png")
                .size(1024L)
                .build();
        PresignedUrlCommandDto.Response response = PresignedUrlCommandDto.Response.of(
                "https://upload.url", "https://image.url", "posts/key");

        given(postCommandService.createPostImagePresignedUrl(any(PresignedUrlCommandDto.Request.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/editors/posts/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uploadUrl").value("https://upload.url"))
                .andExpect(jsonPath("$.data.imageKey").value("posts/key"));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() throws Exception {
        Long postId = 10L;
        PostCommandDto.UpdateRequest.UpdatePlaceInfoRequest placeInfo = PostCommandDto.UpdateRequest.UpdatePlaceInfoRequest.builder()
                .postPlaceId(77L)
                .placeName("수정 장소")
                .description("수정 설명")
                .addressName("서울 종로구")
                .roadAddressName("서울 종로구 세종대로")
                .latitude(37.57)
                .longitude(126.98)
                .build();
        PostCommandDto.UpdateRequest request = PostCommandDto.UpdateRequest.builder()
                .url("https://www.instagram.com/p/updated")
                .hashTags(java.util.List.of("#수정"))
                .placeInfoRequestList(java.util.List.of(placeInfo))
                .build();

        PostCommandDto.Response response = PostCommandDto.Response.builder()
                .postId(postId)
                .url("https://www.instagram.com/p/updated")
                .hashTags(java.util.List.of("#수정"))
                .placeInfoResponseList(Collections.emptyList())
                .build();

        given(postCommandService.updatePost(eq(postId), any(PostCommandDto.UpdateRequest.class),
                eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/editors/me/posts/{postId}", postId)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(10L));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() throws Exception {
        Long postId = 10L;
        java.util.UUID editorId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(delete("/api/v1/editors/me/posts/{postId}", postId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(postCommandService).deletePost(postId, editorId);
    }

    @Test
    @DisplayName("게시글 생성 실패 - URL 누락")
    void createPost_Fail_NoUrl() throws Exception {
        PostCommandDto.CreateRequest request = PostCommandDto.CreateRequest.builder()
                .url("")
                .hashTags(java.util.List.of("#테스트"))
                .placeInfoRequestList(Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/v1/editors/posts")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 생성 실패 - postPlaceId 포함")
    void createPost_Fail_WithPostPlaceId() throws Exception {
        PostCommandDto.CreateRequest.CreatePlaceInfoRequest placeInfo = PostCommandDto.CreateRequest.CreatePlaceInfoRequest.builder()
                .postPlaceId(99L)
                .placeName("테스트 장소")
                .description("테스트 설명")
                .addressName("서울 노원구 공릉동 596-12")
                .roadAddressName("인천 중구 백운로228번길 81-10")
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        PostCommandDto.CreateRequest request = PostCommandDto.CreateRequest.builder()
                .url("https://www.instagram.com/post")
                .hashTags(java.util.List.of("#테스트"))
                .placeInfoRequestList(Collections.singletonList(placeInfo))
                .build();

        given(postCommandService.createPost(any(PostCommandDto.CreateRequest.class), any()))
                .willThrow(new DomainException(PostErrorCode.POST_INVALID_CREATE_POST_PLACE_ID));

        mockMvc.perform(post("/api/v1/editors/posts")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POST_INVALID_CREATE_POST_PLACE_ID"));
    }
}
