package zero.conflict.archiview.user.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.FollowRepository;
import zero.conflict.archiview.user.application.port.out.PostClient;
import zero.conflict.archiview.user.application.port.out.SearchHistoryRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Follow;
import zero.conflict.archiview.user.domain.Hashtags;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.SearchDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArchiverSearchQueryService 테스트")
class ArchiverSearchQueryServiceTest {

    @InjectMocks
    private ArchiverSearchQueryService archiverSearchQueryService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EditorProfileRepository editorProfileRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private SearchHistoryRepository searchHistoryRepository;
    @Mock
    private PostClient postClient;

    @Test
    @DisplayName("검색(ALL) - 장소/에디터 결과를 반환한다")
    void search_all_success() {
        UUID archiverId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));
        given(searchHistoryRepository.findByArchiverIdAndKeywordNormalized(any(), any())).willReturn(Optional.empty());
        given(searchHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(searchHistoryRepository.findAllByArchiverIdOrderByLastModifiedAtDesc(archiverId)).willReturn(List.of());

        PostClient.PostPlaceView postPlace = new PostClient.PostPlaceView(
                1000L,
                editorId,
                10L,
                "용산 카페",
                "서울 용산구 한강로",
                "서울 용산구 한강대로",
                "용산 분위기 맛집",
                "https://img.url",
                "https://www.instagram.com/p/abc",
                List.of("#용산구"),
                20L,
                100L,
                LocalDateTime.of(2026, 2, 9, 12, 0, 0),
                LocalDateTime.of(2026, 2, 10, 12, 0, 0));
        given(postClient.findAllVisibleByArchiverId(archiverId)).willReturn(List.of(postPlace));

        EditorProfile profile = EditorProfile.builder()
                .user(User.builder().id(editorId).build())
                .nickname("용산에디터")
                .instagramId("editor_yongsan")
                .instagramUrl("https://www.instagram.com/editor_yongsan")
                .introduction("용산 위주로 기록")
                .hashtags(Hashtags.of("#용산구", "#맛집"))
                .build();
        given(editorProfileRepository.findAll()).willReturn(List.of(profile));
        given(editorProfileRepository.findAllByUserIds(any())).willReturn(List.of(profile));
        given(followRepository.findAllByArchiverId(archiverId))
                .willReturn(List.of(Follow.createOf(archiverId, editorId)));

        SearchDto.Response response = archiverSearchQueryService.search(archiverId, "용산구", SearchDto.Tab.ALL);

        assertThat(response.getPlaceCount()).isEqualTo(1);
        assertThat(response.getEditorCount()).isEqualTo(1);
        assertThat(response.getPlaces().get(0).getPlaceName()).isEqualTo("용산 카페");
        assertThat(response.getEditors().get(0).isFollowing()).isTrue();
    }

    @Test
    @DisplayName("추천 키워드 - count, 최신, 가나다 순으로 정렬")
    void recommendations_sorted() {
        UUID archiverId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));

        EditorProfile p1 = EditorProfile.builder()
                .user(User.builder().id(UUID.randomUUID()).build())
                .nickname("a")
                .instagramId("a")
                .instagramUrl("https://www.instagram.com/a")
                .introduction("a")
                .hashtags(Hashtags.of("#카페", "#커피맛집"))
                .build();
        setField(p1, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 10, 0, 0));

        EditorProfile p2 = EditorProfile.builder()
                .user(User.builder().id(UUID.randomUUID()).build())
                .nickname("b")
                .instagramId("b")
                .instagramUrl("https://www.instagram.com/b")
                .introduction("b")
                .hashtags(Hashtags.of("#카페", "#브런치"))
                .build();
        setField(p2, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 11, 0, 0));

        given(editorProfileRepository.findAll()).willReturn(List.of(p1, p2));

        SearchDto.RecommendationListResponse response = archiverSearchQueryService.getRecommendations(archiverId);

        assertThat(response.getKeywords().get(0).getKeyword()).isEqualTo("#카페");
        assertThat(response.getKeywords().get(0).getCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("검색 - 아카이버가 아니면 검색할 수 없다")
    void search_fail_when_not_archiver() {
        UUID editorId = UUID.randomUUID();
        given(userRepository.findById(editorId))
                .willReturn(Optional.of(User.builder().id(editorId).role(User.Role.EDITOR).build()));

        assertThatThrownBy(() -> archiverSearchQueryService.search(editorId, "용산", SearchDto.Tab.ALL))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_SEARCHER_ROLE));
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> type = target.getClass();
            while (type != null) {
                try {
                    java.lang.reflect.Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            throw new NoSuchFieldException(fieldName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
