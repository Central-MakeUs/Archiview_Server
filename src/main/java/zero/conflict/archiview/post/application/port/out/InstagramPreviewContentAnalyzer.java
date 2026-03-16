package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.util.List;

public interface InstagramPreviewContentAnalyzer {

    InstagramPreviewDto.ContentAnalysis analyze(String caption, List<InstagramPreviewDto.MediaItem> mediaItems);
}
