package zero.conflict.archiview.post.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface UserClient {

    boolean existsUser(UUID userId);

    boolean existsEditorProfile(UUID userId);

    Map<UUID, EditorSummary> getEditorSummaries(List<UUID> userIds);

    Set<UUID> getBlockedEditorIds(UUID archiverId);

    record EditorSummary(
            UUID userId,
            String nickname,
            String instagramId) {
    }
}
