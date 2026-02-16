package zero.conflict.archiview.user.application.archiver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.user.application.archiver.command.EditorBlockCommandService;
import zero.conflict.archiview.user.application.archiver.command.FollowCommandService;
import zero.conflict.archiview.user.application.archiver.query.ArchiverProfileQueryService;
import zero.conflict.archiview.user.application.archiver.query.ArchiverSearchQueryService;
import zero.conflict.archiview.user.application.archiver.query.BlockQueryService;
import zero.conflict.archiview.user.application.editor.query.EditorProfileQueryService;
import zero.conflict.archiview.user.application.archiver.query.FollowQueryService;
import zero.conflict.archiview.user.application.archiver.query.TrustedEditorQueryService;
import zero.conflict.archiview.user.application.port.in.ArchiverUserUseCase;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;
import zero.conflict.archiview.user.dto.BlockDto;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.FollowDto;
import zero.conflict.archiview.user.dto.SearchDto;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchiverUserService implements ArchiverUserUseCase {

    private final ArchiverProfileQueryService archiverProfileQueryService;
    private final FollowQueryService followQueryService;
    private final BlockQueryService blockQueryService;
    private final TrustedEditorQueryService trustedEditorQueryService;
    private final EditorProfileQueryService editorProfileQueryService;
    private final FollowCommandService followCommandService;
    private final EditorBlockCommandService editorBlockCommandService;
    private final ArchiverSearchQueryService archiverSearchQueryService;

    @Override
    public ArchiverProfileDto.Response getMyProfile(UUID userId) {
        return archiverProfileQueryService.getMyProfile(userId);
    }

    @Override
    public FollowDto.ListResponse getMyFollowings(UUID archiverId) {
        return followQueryService.getMyFollowings(archiverId);
    }

    @Override
    public BlockDto.ListResponse getMyBlockedEditors(UUID archiverId) {
        return blockQueryService.getMyBlockedEditors(archiverId);
    }

    @Override
    public TrustedEditorDto.ListResponse getTrustedEditors() {
        return trustedEditorQueryService.getTrustedEditors();
    }

    @Override
    public EditorProfileDto.ArchiverEditorProfileResponse getEditorProfile(UUID archiverId, UUID editorId) {
        EditorProfileDto.Response response = editorProfileQueryService.getEditorProfile(editorId);
        boolean following = followQueryService.isFollowing(archiverId, editorId);
        return EditorProfileDto.ArchiverEditorProfileResponse.from(response, following);
    }

    @Override
    public void follow(UUID archiverId, UUID editorId) {
        followCommandService.follow(archiverId, editorId);
    }

    @Override
    public void unfollow(UUID archiverId, UUID editorId) {
        followCommandService.unfollow(archiverId, editorId);
    }

    @Override
    public void blockEditor(UUID archiverId, UUID editorId) {
        editorBlockCommandService.blockEditor(archiverId, editorId);
    }

    @Override
    public void unblockEditor(UUID archiverId, UUID editorId) {
        editorBlockCommandService.unblockEditor(archiverId, editorId);
    }

    @Override
    public SearchDto.Response search(UUID archiverId, String query, SearchDto.Tab tab) {
        return archiverSearchQueryService.search(archiverId, query, tab);
    }

    @Override
    public SearchDto.RecentListResponse getRecentSearches(UUID archiverId) {
        return archiverSearchQueryService.getRecentSearches(archiverId);
    }

    @Override
    public void deleteRecentSearch(UUID archiverId, Long historyId) {
        archiverSearchQueryService.deleteRecentSearch(archiverId, historyId);
    }

    @Override
    public SearchDto.RecommendationListResponse getRecommendations(UUID archiverId) {
        return archiverSearchQueryService.getRecommendations(archiverId);
    }
}
