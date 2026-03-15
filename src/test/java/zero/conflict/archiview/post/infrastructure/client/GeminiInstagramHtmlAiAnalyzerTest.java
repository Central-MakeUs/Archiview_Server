package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zero.conflict.archiview.post.application.port.out.InstagramPostExtractor;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiInstagramHtmlAiAnalyzerTest {

    @Test
    @DisplayName("Gemini 응답 JSON을 구조화 결과로 파싱한다")
    void parseResponse_success() throws Exception {
        GeminiInstagramHtmlAiAnalyzer analyzer = new GeminiInstagramHtmlAiAnalyzer(
                new ObjectMapper(),
                "test-key",
                "gemini-test",
                "https://example.com",
                1000);

        String responseText = """
                ```json
                {
                  "caption": "테스트 캡션",
                  "hashTags": ["테스트", "#맛집"],
                  "mediaCandidates": [
                    {"sourceUrl": "https://img.example/1.jpg", "mediaType": "IMAGE"},
                    {"sourceUrl": "https://img.example/2.jpg", "mediaType": "VIDEO"}
                  ],
                  "confidenceNote": "high"
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

        InstagramPostExtractor.ExtractedInstagramPost result =
                analyzer.parseResponse("https://instagram.com/p/test", response);

        assertThat(result.caption()).isEqualTo("테스트 캡션");
        assertThat(result.hashTags()).containsExactly("#테스트", "#맛집");
        assertThat(result.mediaList()).hasSize(2);
        assertThat(result.mediaList().get(0).mediaType()).isEqualTo(InstagramPreviewDto.MediaType.IMAGE);
        assertThat(result.mediaList().get(1).mediaType()).isEqualTo(InstagramPreviewDto.MediaType.VIDEO);
    }
}
