package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.util.List;

public interface InstagramPostExtractor {

    ExtractedInstagramPost extract(String instagramUrl);

    record ExtractedInstagramPost(
            String sourceUrl,
            String caption,
            List<String> hashTags,
            List<ExtractedMedia> mediaList) {
    }

    record ExtractedMedia(
            String sourceUrl,
            InstagramPreviewDto.MediaType mediaType) {
    }
}
