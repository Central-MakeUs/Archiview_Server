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

    @Test
    @DisplayName("릴스 canonical 메타가 있으면 og:video가 없어도 VIDEO로 판정한다")
    void analyzeHtml_marksReelAsVideoWhenCanonicalUrlIsReel() {
        InstagramHtmlAiAnalyzer analyzer = mock(InstagramHtmlAiAnalyzer.class);
        InstagramPostExtractorAdapter adapter = new InstagramPostExtractorAdapter(
                analyzer,
                new ObjectMapper(),
                "test-agent",
                12000);

        String html = """
                <html><head>
                <meta property="og:description" content="reel caption #reel" />
                <meta property="og:image" content="https://fallback.example/reel.jpg" />
                <meta property="og:url" content="https://www.instagram.com/someuser/reel/DUu3vBECQgr/" />
                <meta name="twitter:title" content="someuser • Instagram reel" />
                </head><body>fallback body</body></html>
                """;
        given(analyzer.analyze("https://instagram.com/p/test", adapter.normalizeHtmlForAi(org.jsoup.Jsoup.parse(html))))
                .willThrow(new IllegalStateException("gemini failed"));

        InstagramPostExtractor.ExtractedInstagramPost result = adapter.analyzeHtml("https://instagram.com/p/test", html);

        assertThat(result.mediaList()).hasSize(1);
        assertThat(result.mediaList().get(0).mediaType()).isEqualTo(InstagramPreviewDto.MediaType.VIDEO);
    }

    @Test
    @DisplayName("메인 페이지와 embed 결과를 합칠 때 같은 미디어 URL이면 VIDEO를 우선한다")
    void mergeExtractedPosts_prefersVideoMediaType() {
        InstagramHtmlAiAnalyzer analyzer = mock(InstagramHtmlAiAnalyzer.class);
        InstagramPostExtractorAdapter adapter = new InstagramPostExtractorAdapter(
                analyzer,
                new ObjectMapper(),
                "test-agent",
                12000);

        InstagramPostExtractor.ExtractedInstagramPost primary = new InstagramPostExtractor.ExtractedInstagramPost(
                "https://instagram.com/p/test",
                "caption",
                List.of("#one"),
                List.of(new InstagramPostExtractor.ExtractedMedia(
                        "https://cdn.example/media.jpg",
                        InstagramPreviewDto.MediaType.IMAGE)));
        InstagramPostExtractor.ExtractedInstagramPost secondary = new InstagramPostExtractor.ExtractedInstagramPost(
                "https://instagram.com/p/test",
                null,
                List.of("#two"),
                List.of(new InstagramPostExtractor.ExtractedMedia(
                        "https://cdn.example/media.jpg",
                        InstagramPreviewDto.MediaType.VIDEO)));

        InstagramPostExtractor.ExtractedInstagramPost merged = adapter.mergeExtractedPosts(primary, secondary);

        assertThat(merged.caption()).isEqualTo("caption");
        assertThat(merged.hashTags()).containsExactly("#one", "#two");
        assertThat(merged.mediaList()).hasSize(1);
        assertThat(merged.mediaList().get(0).mediaType()).isEqualTo(InstagramPreviewDto.MediaType.VIDEO);
    }

    @Test
    @DisplayName("oEmbed 응답에서 릴스 캡션과 썸네일을 VIDEO 타입으로 변환한다")
    void parseOEmbed_parsesReelAsVideo() throws Exception {
        InstagramHtmlAiAnalyzer analyzer = mock(InstagramHtmlAiAnalyzer.class);
        ObjectMapper objectMapper = new ObjectMapper();
        InstagramPostExtractorAdapter adapter = new InstagramPostExtractorAdapter(
                analyzer,
                objectMapper,
                "test-agent",
                12000);

        String json = """
                {
                  "title": "reel caption #reel",
                  "thumbnail_url": "https://cdn.example/reel-thumb.jpg",
                  "html": "<blockquote data-instgrm-permalink=\\"https://www.instagram.com/reel/DUu3vBECQgr/?utm_source=ig_embed\\"></blockquote>"
                }
                """;

        InstagramPostExtractor.ExtractedInstagramPost result = adapter.parseOEmbed(
                "https://instagram.com/p/DUu3vBECQgr/",
                objectMapper.readTree(json));

        assertThat(result.caption()).isEqualTo("reel caption #reel");
        assertThat(result.hashTags()).containsExactly("#reel");
        assertThat(result.mediaList()).hasSize(1);
        assertThat(result.mediaList().get(0).sourceUrl()).isEqualTo("https://cdn.example/reel-thumb.jpg");
        assertThat(result.mediaList().get(0).mediaType()).isEqualTo(InstagramPreviewDto.MediaType.VIDEO);
    }
}
