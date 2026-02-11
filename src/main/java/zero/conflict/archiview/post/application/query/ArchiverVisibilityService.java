package zero.conflict.archiview.post.application.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.port.out.PostPlaceReportRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.PostPlaceReport;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiverVisibilityService {

    private final PostPlaceReportRepository postPlaceReportRepository;
    private final UserClient userClient;

    @Transactional(readOnly = true)
    public VisibilityFilter getVisibilityFilter(UUID archiverId) {
        Set<Long> reportedPostPlaceIds = postPlaceReportRepository.findAllByArchiverId(archiverId).stream()
                .map(PostPlaceReport::getPostPlaceId)
                .collect(Collectors.toSet());

        Set<UUID> blockedEditorIds = userClient.getBlockedEditorIds(archiverId);

        return new VisibilityFilter(reportedPostPlaceIds, blockedEditorIds);
    }

    public List<PostPlace> filterVisiblePostPlaces(List<PostPlace> postPlaces, VisibilityFilter visibilityFilter) {
        return postPlaces.stream()
                .filter(postPlace -> isVisible(postPlace, visibilityFilter))
                .toList();
    }

    public boolean isVisible(PostPlace postPlace, VisibilityFilter visibilityFilter) {
        if (postPlace == null) {
            return false;
        }
        if (visibilityFilter.getReportedPostPlaceIds().contains(postPlace.getId())) {
            return false;
        }
        return !visibilityFilter.getBlockedEditorIds().contains(postPlace.getEditorId());
    }

    @Getter
    @RequiredArgsConstructor
    public static class VisibilityFilter {
        private final Set<Long> reportedPostPlaceIds;
        private final Set<UUID> blockedEditorIds;
    }
}
