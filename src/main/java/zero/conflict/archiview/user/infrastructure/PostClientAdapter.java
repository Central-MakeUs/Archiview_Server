package zero.conflict.archiview.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.infrastructure.PostPlaceJpaRepository;
import zero.conflict.archiview.post.infrastructure.PostPlaceReportJpaRepository;
import zero.conflict.archiview.user.application.port.out.PostClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostClientAdapter implements PostClient {

    private final PostPlaceJpaRepository postPlaceJpaRepository;
    private final PostPlaceReportJpaRepository postPlaceReportJpaRepository;
    private final EditorBlockJpaRepository editorBlockJpaRepository;

    @Override
    public Map<UUID, Long> countByEditorIds(List<UUID> editorIds) {
        if (editorIds == null || editorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postPlaceJpaRepository.findAllByEditorIdIn(editorIds).stream()
                .collect(Collectors.groupingBy(PostPlace::getEditorId, Collectors.counting()));
    }

    @Override
    public List<PostPlaceView> findAllVisibleByArchiverId(UUID archiverId) {
        Set<Long> reportedPostPlaceIds = postPlaceReportJpaRepository.findAllByArchiverId(archiverId).stream()
                .map(report -> report.getPostPlaceId())
                .collect(Collectors.toSet());

        Set<UUID> blockedEditorIds = editorBlockJpaRepository.findAllByArchiverId(archiverId).stream()
                .map(block -> block.getEditorId())
                .collect(Collectors.toSet());

        return postPlaceJpaRepository.findAll().stream()
                .filter(postPlace -> !reportedPostPlaceIds.contains(postPlace.getId()))
                .filter(postPlace -> !blockedEditorIds.contains(postPlace.getEditorId()))
                .map(this::toView)
                .toList();
    }

    private PostPlaceView toView(PostPlace postPlace) {
        return new PostPlaceView(
                postPlace.getId(),
                postPlace.getEditorId(),
                postPlace.getPlace() != null ? postPlace.getPlace().getId() : null,
                postPlace.getPlace() != null ? postPlace.getPlace().getName() : null,
                postPlace.getPlace() != null && postPlace.getPlace().getAddress() != null
                        ? postPlace.getPlace().getAddress().getAddressName()
                        : null,
                postPlace.getPlace() != null && postPlace.getPlace().getAddress() != null
                        ? postPlace.getPlace().getAddress().getRoadAddressName()
                        : null,
                postPlace.getDescription(),
                postPlace.getImageUrl(),
                postPlace.getPost() != null ? postPlace.getPost().getUrl() : null,
                postPlace.getPost() != null ? postPlace.getPost().getHashTags() : List.of(),
                postPlace.getSaveCount(),
                postPlace.getViewCount(),
                postPlace.getCreatedAt(),
                postPlace.getLastModifiedAt());
    }
}
