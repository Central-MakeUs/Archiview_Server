package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiInstagramPreviewContentAnalyzerTest {

    @Test
    @DisplayName("Gemini 초안 응답 JSON을 post 초안 결과로 파싱한다")
    void parseResponse_success() throws Exception {
        GeminiInstagramPreviewContentAnalyzer analyzer = new GeminiInstagramPreviewContentAnalyzer(
                new ObjectMapper(),
                new InstagramPromptTemplateLoader(),
                "test-key",
                "gemini-test",
                "https://example.com",
                1000);

        String responseText = """
                ```json
                {
                  "hashTags": ["#성수맛집", "#데이트"],
                  "draftPlaces": [
                    {
                      "imageIndex": 1,
                      "description": "분위기까지 꽉 찬 성수 데이트 맛집",
                      "categoryIds": [3, 5]
                    }
                  ],
                  "warnings": ["대표 사진은 1장만 선택했습니다."]
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

        GeminiInstagramPreviewContentAnalyzer.GeminiDraftResult result = analyzer.parseResponse(response);

        assertThat(result.hashTags()).containsExactly("#성수맛집", "#데이트");
        assertThat(result.draftPlaces()).hasSize(1);
        assertThat(result.draftPlaces().get(0).getImageIndex()).isEqualTo(1);
        assertThat(result.draftPlaces().get(0).getDescription()).isEqualTo("분위기까지 꽉 찬 성수 데이트 맛집");
        assertThat(result.draftPlaces().get(0).getCategoryIds()).containsExactly(3L, 5L);
        assertThat(result.warnings()).containsExactly("대표 사진은 1장만 선택했습니다.");
    }

    @Test
    @DisplayName("application/octet-stream 응답은 URL 확장자로 이미지 MIME 타입을 보정한다")
    void normalizeMimeType_infersFromUrlWhenOctetStream() {
        GeminiInstagramPreviewContentAnalyzer analyzer = new GeminiInstagramPreviewContentAnalyzer(
                new ObjectMapper(),
                new InstagramPromptTemplateLoader(),
                "test-key",
                "gemini-test",
                "https://example.com",
                1000);

        String mimeType = analyzer.normalizeMimeType(
                "https://cdn.example.com/image/test.webp?X-Amz-Algorithm=AWS4-HMAC-SHA256",
                MediaType.APPLICATION_OCTET_STREAM);

        assertThat(mimeType).isEqualTo("image/webp");
    }
}
