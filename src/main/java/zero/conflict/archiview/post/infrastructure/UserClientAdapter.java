package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.user.infrastructure.EditorBlockJpaRepository;
import zero.conflict.archiview.user.infrastructure.EditorProfileJpaRepository;
import zero.conflict.archiview.user.infrastructure.UserJpaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserClientAdapter implements UserClient {

    private final UserJpaRepository userJpaRepository;
    private final EditorProfileJpaRepository editorProfileJpaRepository;
    private final EditorBlockJpaRepository editorBlockJpaRepository;

    @Override
    public boolean existsUser(UUID userId) {
        return userJpaRepository.existsById(userId);
    }

    @Override
    public boolean existsEditorProfile(UUID userId) {
        return editorProfileJpaRepository.existsByUser_Id(userId);
    }

    @Override
    public Map<UUID, EditorSummary> getEditorSummaries(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return editorProfileJpaRepository.findAllByUser_IdIn(userIds).stream()
                .map(profile -> new EditorSummary(
                        profile.getUserId(),
                        profile.getNickname(),
                        profile.getInstagramId()))
                .collect(Collectors.toMap(EditorSummary::userId, Function.identity(), (a, b) -> a));
    }

    @Override
    public Set<UUID> getBlockedEditorIds(UUID archiverId) {
        return editorBlockJpaRepository.findAllByArchiverId(archiverId).stream()
                .map(block -> block.getEditorId())
                .collect(Collectors.toSet());
    }
}
