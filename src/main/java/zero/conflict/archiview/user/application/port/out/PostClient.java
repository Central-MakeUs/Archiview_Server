package zero.conflict.archiview.user.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PostClient {

    Map<UUID, Long> countByEditorIds(List<UUID> editorIds);

    List<PostPlaceView> findAllVisibleByArchiverId(UUID archiverId);
    List<PostPlaceView> findAllForRecommendation();

    record PostPlaceView(
            Long postPlaceId,
            UUID editorId,
            Long placeId,
            String placeName,
            String addressName,
            String roadAddressName,
            String description,
            String imageUrl,
            String postUrl,
            List<String> hashTags,
            Long saveCount,
            Long viewCount,
            LocalDateTime createdAt,
            LocalDateTime lastModifiedAt) {
    }
}
