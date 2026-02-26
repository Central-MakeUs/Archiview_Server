package zero.conflict.archiview.user.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.archiver.query.ArchiverSearchQueryService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
        PostClient.PostPlaceView p1 = postPlace(
                List.of("#카페", "#커피맛집"),
                LocalDateTime.of(2026, 2, 10, 9, 0, 0),
                LocalDateTime.of(2026, 2, 10, 10, 0, 0));
        PostClient.PostPlaceView p2 = postPlace(
                List.of("#카페"),
                LocalDateTime.of(2026, 2, 10, 9, 30, 0),
                LocalDateTime.of(2026, 2, 10, 11, 0, 0));

        EditorProfile profile1 = editorProfile("#카페", "#가나다");
        setField(profile1, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 12, 0, 0));
        EditorProfile profile2 = editorProfile("#나나다", "#다라마바사");
        setField(profile2, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 12, 0, 0));

        given(postClient.findAllForRecommendation()).willReturn(List.of(p1, p2));
        given(editorProfileRepository.findAll()).willReturn(List.of(profile1, profile2));

        SearchDto.RecommendationListResponse response = archiverSearchQueryService.getRecommendations(archiverId);

        assertThat(response.getKeywords().get(0).getKeyword()).isEqualTo("#카페");
        assertThat(response.getKeywords().get(0).getCount()).isEqualTo(3L);
        assertThat(response.getKeywords().get(1).getKeyword()).isEqualTo("#가나다");
        assertThat(response.getKeywords().get(2).getKeyword()).isEqualTo("#나나다");
        assertThat(response.getKeywords().get(3).getKeyword()).isEqualTo("#다라마바사");
    }

    @Test
    @DisplayName("추천 키워드 - post 시각이 null이어도 예외 없이 집계한다")
    void recommendations_with_null_updated_at() {
        UUID archiverId = UUID.randomUUID();
        given(userRepository.findById(archiverId))
                .willReturn(Optional.of(User.builder().id(archiverId).role(User.Role.ARCHIVER).build()));

        PostClient.PostPlaceView withoutDate = postPlace(
                Arrays.asList("#카페", null, " ", "#디저트"),
                null,
                null);
        PostClient.PostPlaceView withDate = postPlace(
                List.of("#카페", "#브런치"),
                LocalDateTime.of(2026, 2, 10, 9, 30, 0),
                LocalDateTime.of(2026, 2, 10, 11, 0, 0));

        EditorProfile profileWithoutDate = editorProfile("#카페", "#로컬");
        EditorProfile profileWithDate = editorProfile("#카페", "#에디터추천");
        setField(profileWithDate, "lastModifiedAt", LocalDateTime.of(2026, 2, 10, 11, 30, 0));

        given(postClient.findAllForRecommendation()).willReturn(List.of(withoutDate, withDate));
        given(editorProfileRepository.findAll()).willReturn(List.of(profileWithoutDate, profileWithDate));

        assertThatCode(() -> archiverSearchQueryService.getRecommendations(archiverId))
                .doesNotThrowAnyException();

        SearchDto.RecommendationListResponse response = archiverSearchQueryService.getRecommendations(archiverId);
        assertThat(response.getKeywords().get(0).getKeyword()).isEqualTo("#카페");
        assertThat(response.getKeywords().get(0).getCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("검색 - GUEST는 검색할 수 없다")
    void search_fail_when_guest() {
        UUID guestId = UUID.randomUUID();
        given(userRepository.findById(guestId))
                .willReturn(Optional.of(User.builder().id(guestId).role(User.Role.GUEST).build()));

        assertThatThrownBy(() -> archiverSearchQueryService.search(guestId, "용산", SearchDto.Tab.ALL))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_SEARCHER_ROLE));
    }

    @Test
    @DisplayName("검색 - EDITOR도 검색할 수 있다")
    void search_success_when_editor() {
        UUID editorId = UUID.randomUUID();
        UUID targetEditorId = UUID.randomUUID();
        given(userRepository.findById(editorId))
                .willReturn(Optional.of(User.builder().id(editorId).role(User.Role.EDITOR).build()));
        given(searchHistoryRepository.findByArchiverIdAndKeywordNormalized(any(), any())).willReturn(Optional.empty());
        given(searchHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(searchHistoryRepository.findAllByArchiverIdOrderByLastModifiedAtDesc(editorId)).willReturn(List.of());

        PostClient.PostPlaceView postPlace = new PostClient.PostPlaceView(
                1000L,
                targetEditorId,
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
        given(postClient.findAllVisibleByArchiverId(editorId)).willReturn(List.of(postPlace));

        EditorProfile profile = EditorProfile.builder()
                .user(User.builder().id(targetEditorId).build())
                .nickname("용산에디터")
                .instagramId("editor_yongsan")
                .instagramUrl("https://www.instagram.com/editor_yongsan")
                .introduction("용산 위주로 기록")
                .hashtags(Hashtags.of("#용산구", "#맛집"))
                .build();
        given(editorProfileRepository.findAllByUserIds(any())).willReturn(List.of(profile));
        given(followRepository.findAllByArchiverId(editorId)).willReturn(List.of());

        SearchDto.Response response = archiverSearchQueryService.search(editorId, "용산구", SearchDto.Tab.ALL);

        assertThat(response.getPlaceCount()).isEqualTo(1);
        assertThat(response.getEditorCount()).isEqualTo(1);
    }

    private PostClient.PostPlaceView postPlace(List<String> hashTags, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        return new PostClient.PostPlaceView(
                1000L,
                UUID.randomUUID(),
                10L,
                "place",
                "addr",
                "road",
                "desc",
                "https://img.url",
                "https://www.instagram.com/p/abc",
                hashTags,
                20L,
                100L,
                createdAt,
                lastModifiedAt);
    }

    private EditorProfile editorProfile(String primaryTag, String secondaryTag) {
        return EditorProfile.builder()
                .user(User.builder().id(UUID.randomUUID()).build())
                .nickname("editor")
                .instagramId("editor_id")
                .instagramUrl("https://www.instagram.com/editor")
                .introduction("intro")
                .hashtags(Hashtags.of(primaryTag, secondaryTag))
                .build();
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
