package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.dto.InstagramPreviewDto;

public interface InstagramMediaStorage {

    StoredMedia store(InstagramPostExtractor.ExtractedMedia media);

    record StoredMedia(
            String sourceUrl,
            String storedUrl,
            InstagramPreviewDto.MediaType mediaType) {
    }
}
