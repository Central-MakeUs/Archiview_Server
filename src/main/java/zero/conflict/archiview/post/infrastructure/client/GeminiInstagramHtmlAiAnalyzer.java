package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import zero.conflict.archiview.post.application.port.out.InstagramHtmlAiAnalyzer;
import zero.conflict.archiview.post.application.port.out.InstagramPostExtractor;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.net.http.HttpClient;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class GeminiInstagramHtmlAiAnalyzer implements InstagramHtmlAiAnalyzer {

    private static final int MAX_MEDIA_COUNT = 10;
    private static final int MAX_HASHTAG_COUNT = 3;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiInstagramHtmlAiAnalyzer(
            ObjectMapper objectMapper,
            @Value("${instagram.ai.gemini.api-key:}") String apiKey,
            @Value("${instagram.ai.gemini.model:gemini-2.0-flash}") String model,
            @Value("${instagram.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${instagram.ai.gemini.timeout-ms:5000}") long timeoutMs) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(timeoutMs))
                        .build());
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public InstagramPostExtractor.ExtractedInstagramPost analyze(String sourceUrl, String normalizedHtml) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("models", model + ":generateContent")
                .queryParam("key", apiKey)
                .build(true)
                .toUri();

        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", buildPrompt(sourceUrl, normalizedHtml))))),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "responseMimeType", "application/json"));

        try {
            JsonNode response = restClient.post()
                    .uri(uri)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            return parseResponse(sourceUrl, response);
        } catch (RestClientException | JsonProcessingException e) {
            log.warn("Gemini analysis failed. url={}", sourceUrl, e);
            throw new IllegalStateException("Gemini analysis failed", e);
        }
    }

    InstagramPostExtractor.ExtractedInstagramPost parseResponse(String sourceUrl, JsonNode response)
            throws JsonProcessingException {
        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini response text is empty");
        }

        JsonNode root = objectMapper.readTree(stripCodeFence(text));
        String caption = readNullableText(root, "caption");
        List<String> hashTags = sanitizeHashTags(root.path("hashTags"));
        List<InstagramPostExtractor.ExtractedMedia> mediaList = parseMedia(root.path("mediaCandidates"));

        return new InstagramPostExtractor.ExtractedInstagramPost(sourceUrl, caption, hashTags, mediaList);
    }

    private String extractText(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (JsonNode part : parts) {
            JsonNode textNode = part.get("text");
            if (textNode != null && textNode.isTextual()) {
                builder.append(textNode.asText());
            }
        }
        return builder.toString();
    }

    private List<InstagramPostExtractor.ExtractedMedia> parseMedia(JsonNode mediaCandidates) {
        if (!mediaCandidates.isArray()) {
            return List.of();
        }
        List<InstagramPostExtractor.ExtractedMedia> mediaList = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode media : mediaCandidates) {
            if (mediaList.size() >= MAX_MEDIA_COUNT) {
                break;
            }
            String sourceUrl = readNullableText(media, "sourceUrl");
            if (sourceUrl == null || !sourceUrl.startsWith("http") || !seen.add(sourceUrl)) {
                continue;
            }
            String mediaType = readNullableText(media, "mediaType");
            InstagramPreviewDto.MediaType type = "VIDEO".equalsIgnoreCase(mediaType)
                    ? InstagramPreviewDto.MediaType.VIDEO
                    : InstagramPreviewDto.MediaType.IMAGE;
            mediaList.add(new InstagramPostExtractor.ExtractedMedia(sourceUrl, type));
        }
        return mediaList;
    }

    private List<String> sanitizeHashTags(JsonNode hashTagsNode) {
        if (!hashTagsNode.isArray()) {
            return List.of();
        }
        Set<String> hashTags = new LinkedHashSet<>();
        for (JsonNode node : hashTagsNode) {
            if (hashTags.size() >= MAX_HASHTAG_COUNT) {
                break;
            }
            if (!node.isTextual()) {
                continue;
            }
            String value = node.asText().trim();
            if (value.isBlank()) {
                continue;
            }
            if (!value.startsWith("#")) {
                value = "#" + value;
            }
            hashTags.add(value);
        }
        return List.copyOf(hashTags);
    }

    private String readNullableText(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || !field.isTextual()) {
            return null;
        }
        String value = field.asText().trim();
        return value.isBlank() ? null : value;
    }

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstNewLine = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstNewLine < 0 || lastFence <= firstNewLine) {
            return trimmed;
        }
        return trimmed.substring(firstNewLine + 1, lastFence).trim();
    }

    private String buildPrompt(String sourceUrl, String normalizedHtml) {
        return """
                You analyze Instagram post HTML and must return JSON only.
                Do not guess missing values.
                Do not return markdown.
                Exclude place/address/coordinate information.
                Limit hashtags to at most 3.
                mediaCandidates must contain only direct media or thumbnail URLs.
                Use this JSON shape exactly:
                {
                  "caption": string|null,
                  "hashTags": string[],
                  "mediaCandidates": [{"sourceUrl": string, "mediaType": "IMAGE"|"VIDEO"}],
                  "confidenceNote": string|null
                }

                Source URL:
                %s

                Normalized HTML:
                %s
                """.formatted(sourceUrl, normalizedHtml);
    }
}
