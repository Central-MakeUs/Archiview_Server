package zero.conflict.archiview.post.application.port.out;

public interface InstagramHtmlAiAnalyzer {

    InstagramPostExtractor.ExtractedInstagramPost analyze(String sourceUrl, String normalizedHtml);
}
