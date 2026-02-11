package zero.conflict.archiview.user.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.FollowRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Follow;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.FollowDto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final EditorProfileRepository editorProfileRepository;

    public FollowDto.ListResponse getMyFollowings(UUID archiverId) {
        User archiver = userRepository.findById(archiverId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        if (archiver.getRole() != User.Role.ARCHIVER) {
            throw new DomainException(UserErrorCode.INVALID_FOLLOWER_ROLE);
        }

        List<Follow> follows = followRepository.findAllByArchiverId(archiverId);
        if (follows.isEmpty()) {
            return FollowDto.ListResponse.empty();
        }

        List<UUID> editorIds = follows.stream()
                .sorted(Comparator.comparing(Follow::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(Follow::getEditorId)
                .toList();

        List<EditorProfile> profiles = editorProfileRepository.findAllByUserIds(editorIds);
        if (profiles.size() != editorIds.size()) {
            throw new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND);
        }

        Map<UUID, Integer> orderByEditorId = new HashMap<>();
        for (int index = 0; index < editorIds.size(); index++) {
            orderByEditorId.put(editorIds.get(index), index);
        }

        List<FollowDto.FollowingResponse> editors = profiles.stream()
                .sorted(Comparator.comparing(
                        profile -> orderByEditorId.getOrDefault(profile.getUser().getId(), Integer.MAX_VALUE)))
                .map(FollowDto.FollowingResponse::from)
                .toList();

        return FollowDto.ListResponse.from(editors);
    }
}
