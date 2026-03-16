package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import zero.conflict.archiview.post.application.port.out.InstagramPreviewContentAnalyzer;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class GeminiInstagramPreviewContentAnalyzer implements InstagramPreviewContentAnalyzer {

    private static final int MAX_IMAGE_COUNT = 3;

    private final ObjectMapper objectMapper;
    private final RestClient geminiRestClient;
    private final RestClient mediaRestClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiInstagramPreviewContentAnalyzer(
            ObjectMapper objectMapper,
            @Value("${instagram.ai.gemini.api-key:}") String apiKey,
            @Value("${instagram.ai.gemini.model:gemini-3-flash-preview}") String model,
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
        this.geminiRestClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.mediaRestClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public InstagramPreviewDto.ContentAnalysis analyze(String caption, List<InstagramPreviewDto.MediaItem> mediaItems) {
        List<String> warnings = new ArrayList<>();
        String captionSummary = summarizeCaption(caption);
        List<ImagePart> imageParts = downloadImageParts(mediaItems, warnings);
        boolean hasVideo = mediaItems != null && mediaItems.stream()
                .anyMatch(item -> item.getMediaType() == InstagramPreviewDto.MediaType.VIDEO);

        if (apiKey == null || apiKey.isBlank()) {
            warnings.add("Gemini API 키가 없어 미디어 상세 분석을 건너뛰었습니다.");
            if (hasVideo) {
                warnings.add("공개 인스타그램 응답만으로는 릴스 음성을 직접 전사할 수 없습니다.");
            }
            return buildResult(
                    captionSummary,
                    null,
                    null,
                    null,
                    warnings,
                    imageParts.isEmpty() ? InstagramPreviewDto.AnalysisStatus.SKIPPED
                            : InstagramPreviewDto.AnalysisStatus.PARTIAL_SUCCESS);
        }

        String visibleText = null;
        String sceneDescription = null;
        if (!imageParts.isEmpty()) {
            try {
                GeminiAnalysisResult result = requestGemini(caption, imageParts);
                visibleText = blankToNull(result.visibleText());
                sceneDescription = blankToNull(result.sceneDescription());
            } catch (RuntimeException e) {
                log.info("Gemini media content analysis failed", e);
                warnings.add("이미지 OCR/장면 분석에 실패했습니다.");
            }
        } else {
            warnings.add("분석 가능한 이미지가 없어 OCR/장면 분석을 건너뛰었습니다.");
        }

        String audioTranscript = null;
        if (hasVideo) {
            warnings.add("공개 인스타그램 응답만으로는 릴스 음성을 직접 전사할 수 없습니다.");
        }

        InstagramPreviewDto.AnalysisStatus status = determineStatus(captionSummary, visibleText, sceneDescription, audioTranscript,
                !warnings.isEmpty());
        return buildResult(captionSummary, visibleText, sceneDescription, audioTranscript, warnings, status);
    }

    private String summarizeCaption(String caption) {
        if (caption == null || caption.isBlank()) {
            return null;
        }
        String normalized = caption.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 180) {
            return normalized;
        }
        return normalized.substring(0, 180) + "...";
    }

    private List<ImagePart> downloadImageParts(List<InstagramPreviewDto.MediaItem> mediaItems, List<String> warnings) {
        if (mediaItems == null) {
            return List.of();
        }
        List<ImagePart> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (InstagramPreviewDto.MediaItem mediaItem : mediaItems) {
            if (result.size() >= MAX_IMAGE_COUNT) {
                break;
            }
            String url = mediaItem.getStoredUrl() != null ? mediaItem.getStoredUrl() : mediaItem.getSourceUrl();
            if (url == null || !seen.add(url)) {
                continue;
            }
            try {
                result.add(downloadImage(url));
            } catch (RuntimeException e) {
                warnings.add("일부 미디어를 상세 분석용으로 불러오지 못했습니다.");
                log.info("Failed to download media for Gemini analysis. url={}", url, e);
            }
        }
        return result;
    }

    private ImagePart downloadImage(String url) {
        return mediaRestClient.get()
                .uri(url)
                .exchange((request, response) -> {
                    MediaType contentType = response.getHeaders().getContentType();
                    byte[] bytes = response.getBody().readAllBytes();
                    if (bytes.length == 0) {
                        throw new IllegalStateException("Downloaded media is empty");
                    }
                    String mimeType = contentType == null ? MediaType.IMAGE_JPEG_VALUE : contentType.toString();
                    return new ImagePart(mimeType, Base64.getEncoder().encodeToString(bytes));
                });
    }

    private GeminiAnalysisResult requestGemini(String caption, List<ImagePart> imageParts) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("models", model + ":generateContent")
                .queryParam("key", apiKey)
                .build(true)
                .toUri();

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", buildPrompt(caption)));
        for (ImagePart imagePart : imageParts) {
            parts.add(Map.of("inlineData", Map.of(
                    "mimeType", imagePart.mimeType(),
                    "data", imagePart.base64Data())));
        }

        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", parts)),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "responseMimeType", "application/json"));

        try {
            JsonNode response = geminiRestClient.post()
                    .uri(uri)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            return parseResponse(response);
        } catch (RestClientException | JsonProcessingException e) {
            throw new IllegalStateException("Gemini media analysis failed", e);
        }
    }

    GeminiAnalysisResult parseResponse(JsonNode response) throws JsonProcessingException {
        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini response text is empty");
        }

        JsonNode root = objectMapper.readTree(stripCodeFence(text));
        return new GeminiAnalysisResult(
                readNullableText(root, "visibleText"),
                readNullableText(root, "sceneDescription"));
    }

    private String buildPrompt(String caption) {
        return """
                You analyze Instagram preview media and must return JSON only.
                Do not guess audio content.
                Summarize only what is visibly present in the provided images.
                Use this JSON shape exactly:
                {
                  "visibleText": string|null,
                  "sceneDescription": string|null
                }

                Author caption:
                %s
                """.formatted(caption == null ? "" : caption);
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private InstagramPreviewDto.AnalysisStatus determineStatus(
            String captionSummary,
            String visibleText,
            String sceneDescription,
            String audioTranscript,
            boolean hasWarnings) {
        boolean hasAny = captionSummary != null || visibleText != null || sceneDescription != null || audioTranscript != null;
        if (!hasAny) {
            return hasWarnings ? InstagramPreviewDto.AnalysisStatus.SKIPPED : InstagramPreviewDto.AnalysisStatus.FAILED;
        }
        if (hasWarnings) {
            return InstagramPreviewDto.AnalysisStatus.PARTIAL_SUCCESS;
        }
        return InstagramPreviewDto.AnalysisStatus.SUCCESS;
    }

    private InstagramPreviewDto.ContentAnalysis buildResult(
            String captionSummary,
            String visibleText,
            String sceneDescription,
            String audioTranscript,
            List<String> warnings,
            InstagramPreviewDto.AnalysisStatus status) {
        return InstagramPreviewDto.ContentAnalysis.builder()
                .status(status)
                .captionSummary(captionSummary)
                .visibleText(visibleText)
                .sceneDescription(sceneDescription)
                .audioTranscript(audioTranscript)
                .warnings(List.copyOf(warnings))
                .build();
    }

    private record ImagePart(String mimeType, String base64Data) {
    }

    record GeminiAnalysisResult(
            String visibleText,
            String sceneDescription) {
    }
}
