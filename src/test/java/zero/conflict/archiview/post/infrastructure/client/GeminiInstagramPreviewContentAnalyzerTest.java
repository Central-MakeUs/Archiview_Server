package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiInstagramPreviewContentAnalyzerTest {

    @Test
    @DisplayName("Gemini 미디어 분석 응답 JSON을 구조화 결과로 파싱한다")
    void parseResponse_success() throws Exception {
        GeminiInstagramPreviewContentAnalyzer analyzer = new GeminiInstagramPreviewContentAnalyzer(
                new ObjectMapper(),
                "test-key",
                "gemini-test",
                "https://example.com",
                1000);

        String responseText = """
                ```json
                {
                  "visibleText": "매장명\\n행사중",
                  "sceneDescription": "뷔페 음식이 놓인 실내 장면"
                }
                ```
                """;

        var response = new ObjectMapper().readTree("""
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {"text": %s}
                        ]
                      }
                    }
                  ]
                }
                """.formatted(new ObjectMapper().writeValueAsString(responseText)));

        GeminiInstagramPreviewContentAnalyzer.GeminiAnalysisResult result = analyzer.parseResponse(response);

        assertThat(result.visibleText()).isEqualTo("매장명\n행사중");
        assertThat(result.sceneDescription()).isEqualTo("뷔페 음식이 놓인 실내 장면");
    }
}
