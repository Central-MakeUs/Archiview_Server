package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.dto.InstagramPreviewDto;
import zero.conflict.archiview.post.domain.Category;

import java.util.List;

public interface InstagramPreviewContentAnalyzer {

    InstagramPreviewDto.DraftAnalysis analyze(
            String caption,
            List<String> hashTags,
            List<InstagramPreviewDto.MediaItem> mediaItems,
            List<Category> categories);
}
