package zero.conflict.archiview.post.presentation.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.post.application.editor.query.EditorPostQueryService;
import zero.conflict.archiview.post.dto.EditorInsightDto;
import zero.conflict.archiview.post.dto.EditorMapDto;
import zero.conflict.archiview.post.dto.EditorPostByPostPlaceDto;
import zero.conflict.archiview.post.dto.EditorUploadedPlaceDto;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditorPostQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private EditorPostQueryService editorPostQueryService;

    @Test
    @DisplayName("에디터 인사이트 요약 조회 - 성공")
    void getInsightSummary_success() throws Exception {
        EditorInsightDto.SummaryResponse response = EditorInsightDto.SummaryResponse.of(
                "에디터A", 10L, 100L, 50L, 1000L, EditorInsightDto.Period.ALL);
        given(editorPostQueryService.getInsightSummary(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(EditorInsightDto.Period.ALL)
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/editors/me/insights/summary")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.editorNickname").value("에디터A"))
                .andExpect(jsonPath("$.data.totalPlaceCount").value(10));
    }

    @Test
    @DisplayName("에디터 인사이트 장소 목록 조회 - 기본 정렬")
    void getInsightPlaces_DefaultSort_NoPeriod() throws Exception {
        given(editorPostQueryService.getInsightPlaces(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(EditorInsightDto.PlaceSort.RECENT)))
                .willReturn(EditorInsightDto.PlaceCardListResponse.empty(EditorInsightDto.PlaceSort.RECENT));

        mockMvc.perform(get("/api/v1/editors/me/insights/places")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sort").value("RECENT"))
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

        given(editorPostQueryService.getInsightPlaceDetail(org.mockito.ArgumentMatchers.any(),
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
    @DisplayName("에디터 지도 핀 조회 - 성공")
    void getMapPins_success() throws Exception {
        EditorMapDto.PlacePinResponse pin = EditorMapDto.PlacePinResponse.builder()
                .placeId(1L)
                .name("서울숲 브루어스")
                .latitude(37.5468)
                .longitude(127.0437)
                .categories(List.of("카페"))
                .build();
        EditorMapDto.Response response = EditorMapDto.Response.from(List.of(pin));

        given(editorPostQueryService.getMapPins(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(EditorMapDto.MapFilter.ALL),
                org.mockito.ArgumentMatchers.eq(List.of(1L, 2L))
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/editors/me/map/places")
                        .with(authenticatedUser())
                        .queryParam("filter", "ALL")
                        .queryParam("categoryIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pins[0].placeId").value(1L))
                .andExpect(jsonPath("$.data.pins[0].name").value("서울숲 브루어스"));
    }

    @Test
    @DisplayName("에디터 업로드 장소 목록 조회 - 성공")
    void getUploadedPlaces_success() throws Exception {
        EditorUploadedPlaceDto.PlaceCardResponse place = EditorUploadedPlaceDto.PlaceCardResponse.builder()
                .placeId(101L)
                .placeName("연남동 밀크티바")
                .editorSummary("요약")
                .stats(EditorUploadedPlaceDto.Stats.builder().viewCount(10L).saveCount(5L).build())
                .build();
        EditorUploadedPlaceDto.ListResponse response = EditorUploadedPlaceDto.ListResponse.from(List.of(place));

        given(editorPostQueryService.getUploadedPlaces(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(get("/api/v1/editors/me/places")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.places[0].placeId").value(101L))
                .andExpect(jsonPath("$.data.places[0].placeName").value("연남동 밀크티바"));
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

        given(editorPostQueryService.getPostByPostPlaceId(postPlaceId)).willReturn(response);

        mockMvc.perform(get("/api/v1/editors/me/posts/by-post-place/{postPlaceId}", postPlaceId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(10L))
                .andExpect(jsonPath("$.data.postPlaces[0].postPlaceId").value(100L))
                .andExpect(jsonPath("$.data.postPlaces[0].placeUrl").value("https://place.url"));
    }
}
