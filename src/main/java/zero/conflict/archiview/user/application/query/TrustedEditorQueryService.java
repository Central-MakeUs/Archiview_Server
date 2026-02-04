package zero.conflict.archiview.user.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.FollowRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Follow;
import zero.conflict.archiview.user.dto.TrustedEditorDto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrustedEditorQueryService {

    private static final int MAX_TRUSTED_EDITORS = 7;

    private final EditorProfileRepository editorProfileRepository;
    private final FollowRepository followRepository;
    private final PostPlaceRepository postPlaceRepository;

    @Transactional(readOnly = true)
    public TrustedEditorDto.ListResponse getTrustedEditors() {
        List<EditorProfile> profiles = editorProfileRepository.findAll();
        if (profiles.isEmpty()) {
            return TrustedEditorDto.ListResponse.empty();
        }

        List<UUID> editorIds = profiles.stream()
                .map(profile -> profile.getUser().getId())
                .toList();

        Map<UUID, Long> followerCounts = countFollowers(editorIds);
        Map<UUID, Long> postPlaceCounts = countPostPlaces(editorIds);

        List<TrustedEditorDto.EditorResponse> editors = profiles.stream()
                .sorted(Comparator
                        .comparing((EditorProfile profile) ->
                                followerCounts.getOrDefault(profile.getUser().getId(), 0L))
                        .reversed()
                        .thenComparing(profile ->
                                postPlaceCounts.getOrDefault(profile.getUser().getId(), 0L), Comparator.reverseOrder()))
                .limit(MAX_TRUSTED_EDITORS)
                .map(TrustedEditorDto.EditorResponse::from)
                .toList();

        return TrustedEditorDto.ListResponse.from(editors);
    }

    private Map<UUID, Long> countFollowers(List<UUID> editorIds) {
        List<Follow> follows = followRepository.findAllByEditorIds(editorIds);
        Map<UUID, Long> counts = new HashMap<>();
        for (Follow follow : follows) {
            counts.merge(follow.getEditorId(), 1L, Long::sum);
        }
        return counts;
    }

    private Map<UUID, Long> countPostPlaces(List<UUID> editorIds) {
        List<PostPlace> postPlaces = postPlaceRepository.findAllByEditorIds(editorIds);
        Map<UUID, Long> counts = new HashMap<>();
        for (PostPlace postPlace : postPlaces) {
            counts.merge(postPlace.getEditorId(), 1L, Long::sum);
        }
        return counts;
    }
}
