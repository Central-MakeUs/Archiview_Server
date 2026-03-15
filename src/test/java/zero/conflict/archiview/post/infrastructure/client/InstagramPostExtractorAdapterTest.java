package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zero.conflict.archiview.post.application.port.out.InstagramHtmlAiAnalyzer;
import zero.conflict.archiview.post.application.port.out.InstagramPostExtractor;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class InstagramPostExtractorAdapterTest {

    @Test
    @DisplayName("Gemini 분석 결과가 유효하면 AI 결과를 우선 사용한다")
    void analyzeHtml_usesAiResultWhenAvailable() {
        InstagramHtmlAiAnalyzer analyzer = mock(InstagramHtmlAiAnalyzer.class);
        InstagramPostExtractorAdapter adapter = new InstagramPostExtractorAdapter(
                analyzer,
                new ObjectMapper(),
                "test-agent",
                12000);

        String html = """
                <html><head>
                <meta property="og:description" content="fallback caption #fallback" />
                <meta property="og:image" content="https://fallback.example/image.jpg" />
                </head><body>fallback body</body></html>
                """;
        given(analyzer.analyze("https://instagram.com/p/test", adapter.normalizeHtmlForAi(org.jsoup.Jsoup.parse(html))))
                .willReturn(new InstagramPostExtractor.ExtractedInstagramPost(
                        "https://instagram.com/p/test",
                        "ai caption #ai",
                        List.of("#ai"),
                        List.of(new InstagramPostExtractor.ExtractedMedia(
                                "https://ai.example/image.jpg",
                                InstagramPreviewDto.MediaType.IMAGE))));

        InstagramPostExtractor.ExtractedInstagramPost result = adapter.analyzeHtml("https://instagram.com/p/test", html);

        assertThat(result.caption()).isEqualTo("ai caption #ai");
        assertThat(result.hashTags()).containsExactly("#ai");
        assertThat(result.mediaList()).hasSize(1);
        assertThat(result.mediaList().get(0).sourceUrl()).isEqualTo("https://ai.example/image.jpg");
    }

    @Test
    @DisplayName("Gemini 분석 실패 시 기존 HTML 규칙 파싱으로 fallback 한다")
    void analyzeHtml_fallsBackToRuleParsingWhenAiFails() {
        InstagramHtmlAiAnalyzer analyzer = mock(InstagramHtmlAiAnalyzer.class);
        InstagramPostExtractorAdapter adapter = new InstagramPostExtractorAdapter(
                analyzer,
                new ObjectMapper(),
                "test-agent",
                12000);

        String html = """
                <html><head>
                <meta property="og:description" content="fallback caption #fallback" />
                <meta property="og:image" content="https://fallback.example/image.jpg" />
                </head><body>fallback body</body></html>
                """;
        given(analyzer.analyze("https://instagram.com/p/test", adapter.normalizeHtmlForAi(org.jsoup.Jsoup.parse(html))))
                .willThrow(new IllegalStateException("gemini failed"));

        InstagramPostExtractor.ExtractedInstagramPost result = adapter.analyzeHtml("https://instagram.com/p/test", html);

        assertThat(result.caption()).isEqualTo("fallback caption #fallback");
        assertThat(result.hashTags()).containsExactly("#fallback");
        assertThat(result.mediaList()).hasSize(1);
        assertThat(result.mediaList().get(0).sourceUrl()).isEqualTo("https://fallback.example/image.jpg");
    }
}
