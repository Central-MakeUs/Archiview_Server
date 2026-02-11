package zero.conflict.archiview.user.application.port.in;

import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.BlockDto;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;
import zero.conflict.archiview.user.dto.SearchDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

import java.util.UUID;

public interface ArchiverUserUseCase {

    ArchiverProfileDto.Response getMyProfile(UUID userId);

    FollowDto.ListResponse getMyFollowings(UUID archiverId);

    BlockDto.ListResponse getMyBlockedEditors(UUID archiverId);

    TrustedEditorDto.ListResponse getTrustedEditors();

    EditorProfileDto.Response getEditorProfile(UUID editorId);

    void follow(UUID archiverId, UUID editorId);

    void unfollow(UUID archiverId, UUID editorId);

    void blockEditor(UUID archiverId, UUID editorId);

    void unblockEditor(UUID archiverId, UUID editorId);

    SearchDto.Response search(UUID archiverId, String query, SearchDto.Tab tab);

    SearchDto.RecentListResponse getRecentSearches(UUID archiverId);

    void deleteRecentSearch(UUID archiverId, Long historyId);

    SearchDto.RecommendationListResponse getRecommendations(UUID archiverId);
}
