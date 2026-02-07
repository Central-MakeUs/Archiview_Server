package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.query.PostQueryService;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class EditorPostQueryControllerTest extends ControllerTestSupport {

        @MockBean
        private PostQueryService postQueryService;

        @Test
        @DisplayName("에디터 인사이트 장소 목록 조회 - 기본 정렬 및 기간 필터 없음")
        void getInsightPlaces_DefaultSort_NoPeriod() throws Exception {
                given(postQueryService.getInsightPlaces(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.eq(EditorInsightDto.PlaceSort.RECENT)))
                                .willReturn(EditorInsightDto.PlaceCardListResponse
                                                .empty(EditorInsightDto.PlaceSort.RECENT));

                mockMvc.perform(get("/api/v1/editors/me/insights/places")
                                .with(authenticatedUser()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.sort").value("RECENT"))
                                .andExpect(jsonPath("$.data.period").doesNotExist())
                                .andExpect(jsonPath("$.data.places").isArray())
                                .andExpect(jsonPath("$.data.places").isEmpty());
        }

        @Test
        @DisplayName("에디터 인사이트 장소 상세 조회 - 성공")
        void getInsightPlaceDetail_Success() throws Exception {
                Long placeId = 1L;
                EditorInsightDto.PostPlaceDetailResponse postPlaceDetail = EditorInsightDto.PostPlaceDetailResponse.of(
                                100L, "에디터", "editor_insta", "https://www.instagram.com/post", List.of("#태그", "#맛집"),
                                "설명", List.of("카테고리"));
                EditorInsightDto.PlaceDetailResponse response = EditorInsightDto.PlaceDetailResponse.of(placeId,
                                List.of(postPlaceDetail));

                given(postQueryService.getInsightPlaceDetail(org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.eq(placeId)))
                                .willReturn(response);

                mockMvc.perform(get("/api/v1/editors/me/places/{placeId}", placeId)
                                .with(authenticatedUser()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.placeId").value(placeId))
                                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(100L))
                                .andExpect(jsonPath("$.data.postPlaces[0].editorName").value("에디터"));
        }

        @Test
        @DisplayName("postPlaceId로 게시글 상세 조회 - 성공")
        void getPostByPostPlaceId_Success() throws Exception {
                Long postPlaceId = 100L;
                EditorPostByPostPlaceDto.PostPlaceResponse postPlace = EditorPostByPostPlaceDto.PostPlaceResponse
                                .builder()
                                .postPlaceId(postPlaceId)
                                .description("설명")
                                .imageUrl("https://img.url")
                                .placeId(1L)
                                .placeName("장소명")
                                .placeUrl("https://place.url")
                                .phoneNumber("02-1234-5678")
                                .categoryIds(List.of(1L, 2L))
                                .categoryNames(List.of("카페", "디저트"))
                                .build();
                EditorPostByPostPlaceDto.Response response = EditorPostByPostPlaceDto.Response.builder()
                                .postId(10L)
                                .url("https://www.instagram.com/p/test")
                                .hashTags(List.of("#성수"))
                                .postPlaces(List.of(postPlace))
                                .build();

                given(postQueryService.getPostByPostPlaceId(postPlaceId)).willReturn(response);

                mockMvc.perform(get("/api/v1/editors/me/posts/by-post-place/{postPlaceId}", postPlaceId)
                                .with(authenticatedUser()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.postId").value(10L))
                                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(100L))
                                .andExpect(jsonPath("$.data.postPlaces[0].placeUrl").value("https://place.url"))
                                .andExpect(jsonPath("$.data.postPlaces[0].phoneNumber").value("02-1234-5678"));
        }
}
