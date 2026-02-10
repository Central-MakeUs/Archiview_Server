package zero.conflict.archiview.user.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import zero.conflict.archiview.ControllerTestSupport;
import zero.conflict.archiview.user.application.query.ArchiverSearchQueryService;
import zero.conflict.archiview.user.dto.SearchDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArchiverSearchQueryControllerTest extends ControllerTestSupport {

    @MockBean
    private ArchiverSearchQueryService archiverSearchQueryService;

    @Test
    @DisplayName("검색 조회 성공")
    void search_success() throws Exception {
        SearchDto.Response response = SearchDto.Response.builder()
                .query("용산구")
                .tab(SearchDto.Tab.ALL)
                .placeCount(1)
                .editorCount(1)
                .hasMorePlaces(false)
                .hasMoreEditors(false)
                .places(List.of(SearchDto.PlaceCard.builder()
                        .placeId(1L)
                        .placeName("용산 카페")
                        .build()))
                .editors(List.of(SearchDto.EditorCard.builder()
                        .editorId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000201"))
                        .nickname("에디터")
                        .following(true)
                        .build()))
                .build();
        given(archiverSearchQueryService.search(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("용산구"),
                org.mockito.ArgumentMatchers.eq(SearchDto.Tab.ALL))).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/search")
                        .with(authenticatedUser())
                        .queryParam("q", "용산구")
                        .queryParam("tab", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.placeCount").value(1))
                .andExpect(jsonPath("$.data.editorCount").value(1))
                .andExpect(jsonPath("$.data.places[0].placeName").value("용산 카페"));
    }

    @Test
    @DisplayName("최근 검색어 조회 성공")
    void getRecentSearches_success() throws Exception {
        SearchDto.RecentListResponse response = SearchDto.RecentListResponse.from(List.of(
                SearchDto.RecentItem.builder()
                        .historyId(1L)
                        .keyword("용산구")
                        .displayKeyword("용산구")
                        .keywordType(SearchDto.KeywordType.KEYWORD)
                        .searchedAt(LocalDateTime.of(2026, 2, 10, 11, 0, 0))
                        .build()));
        given(archiverSearchQueryService.getRecentSearches(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/search/recent")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.histories[0].keyword").value("용산구"));
    }

    @Test
    @DisplayName("최근 검색어 삭제 성공")
    void deleteRecentSearch_success() throws Exception {
        doNothing().when(archiverSearchQueryService).deleteRecentSearch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(1L));

        mockMvc.perform(delete("/api/v1/archivers/search/recent/{historyId}", 1L)
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("추천 키워드 조회 성공")
    void getRecommendations_success() throws Exception {
        SearchDto.RecommendationListResponse response = SearchDto.RecommendationListResponse.from(List.of(
                SearchDto.RecommendationItem.builder()
                        .keyword("#카페")
                        .count(10L)
                        .latestUsedAt(LocalDateTime.of(2026, 2, 10, 11, 0, 0))
                        .build()));
        given(archiverSearchQueryService.getRecommendations(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(get("/api/v1/archivers/search/recommendations")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.keywords[0].keyword").value("#카페"));
    }
}
