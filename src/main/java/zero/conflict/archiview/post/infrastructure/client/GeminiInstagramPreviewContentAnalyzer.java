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
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GeminiInstagramPreviewContentAnalyzer implements InstagramPreviewContentAnalyzer {

    private static final int MAX_IMAGE_COUNT = 3;
    private static final int MAX_HASHTAG_COUNT = 3;
    private static final int MAX_DESCRIPTION_LENGTH = 50;
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("\\.([a-zA-Z0-9]+)(?:\\?|$)");

    private final ObjectMapper objectMapper;
    private final RestClient geminiRestClient;
    private final RestClient mediaRestClient;
    private final InstagramPromptTemplateLoader promptTemplateLoader;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiInstagramPreviewContentAnalyzer(
            ObjectMapper objectMapper,
            InstagramPromptTemplateLoader promptTemplateLoader,
            @Value("${instagram.ai.gemini.api-key:}") String apiKey,
            @Value("${instagram.ai.gemini.model:gemini-3-flash-preview}") String model,
            @Value("${instagram.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${instagram.ai.gemini.timeout-ms:5000}") long timeoutMs) {
        this.objectMapper = objectMapper;
        this.promptTemplateLoader = promptTemplateLoader;
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
    public InstagramPreviewDto.DraftAnalysis analyze(
            String caption,
            List<String> hashTags,
            List<InstagramPreviewDto.MediaItem> mediaItems,
            List<Category> categories) {
        List<String> warnings = new ArrayList<>();
        List<ImagePart> imageParts = downloadImageParts(mediaItems, warnings);

        if (apiKey == null || apiKey.isBlank()) {
            warnings.add("Gemini API 키가 없어 휴리스틱 초안으로 대체했습니다.");
            return heuristicDraft(caption, hashTags, mediaItems, categories, warnings);
        }

        try {
            GeminiDraftResult result = requestGemini(caption, hashTags, imageParts, categories);
            return InstagramPreviewDto.DraftAnalysis.builder()
                    .hashTags(normalizeHashTags(result.hashTags(), caption, hashTags))
                    .draftPlaces(normalizeCandidates(result.draftPlaces()))
                    .warnings(mergeWarnings(warnings, result.warnings()))
                    .build();
        } catch (RuntimeException e) {
            log.info("Gemini Instagram draft analysis failed", e);
            warnings.add("AI 초안 생성에 실패해 휴리스틱 초안으로 대체했습니다.");
            return heuristicDraft(caption, hashTags, mediaItems, categories, warnings);
        }
    }

    private InstagramPreviewDto.DraftAnalysis heuristicDraft(
            String caption,
            List<String> hashTags,
            List<InstagramPreviewDto.MediaItem> mediaItems,
            List<Category> categories,
            List<String> warnings) {
        List<InstagramPreviewDto.DraftPlaceCandidate> draftPlaces = new ArrayList<>();
        if (mediaItems != null && !mediaItems.isEmpty()) {
            draftPlaces.add(InstagramPreviewDto.DraftPlaceCandidate.builder()
                    .imageIndex(0)
                    .description(fallbackDescription(caption))
                    .categoryIds(fallbackCategoryIds(caption, categories))
                    .build());
        }

        return InstagramPreviewDto.DraftAnalysis.builder()
                .hashTags(normalizeHashTags(hashTags, caption, hashTags))
                .draftPlaces(draftPlaces)
                .warnings(List.copyOf(warnings))
                .build();
    }

    private String fallbackDescription(String caption) {
        if (caption == null || caption.isBlank()) {
            return "한입부터 다시 생각나는 분위기 맛집";
        }
        String normalized = caption
                .replaceAll("#\\S+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isBlank()) {
            return "사진만 봐도 저장각인 분위기 맛집";
        }
        return limitLength(normalized);
    }

    private List<Long> fallbackCategoryIds(String caption, List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        String normalizedCaption = caption == null ? "" : caption.toLowerCase(Locale.ROOT);
        List<Long> matches = categories.stream()
                .filter(category -> normalizedCaption.contains(category.getName().toLowerCase(Locale.ROOT)))
                .map(Category::getId)
                .filter(Objects::nonNull)
                .distinct()
                .limit(2)
                .toList();
        if (!matches.isEmpty()) {
            return matches;
        }
        return categories.stream()
                .map(Category::getId)
                .filter(Objects::nonNull)
                .limit(1)
                .toList();
    }

    private List<ImagePart> downloadImageParts(List<InstagramPreviewDto.MediaItem> mediaItems, List<String> warnings) {
        if (mediaItems == null) {
            return List.of();
        }
        List<ImagePart> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        int mediaIndex = 0;
        for (InstagramPreviewDto.MediaItem mediaItem : mediaItems) {
            if (result.size() >= MAX_IMAGE_COUNT) {
                break;
            }
            String url = mediaItem.getStoredUrl() != null ? mediaItem.getStoredUrl() : mediaItem.getSourceUrl();
            if (url == null || !seen.add(url)) {
                mediaIndex++;
                continue;
            }
            try {
                result.add(downloadImage(url, mediaIndex));
            } catch (RuntimeException e) {
                warnings.add("일부 미디어를 AI 분석용으로 불러오지 못했습니다.");
                log.info("Failed to download media for Gemini analysis. url={}", url, e);
            }
            mediaIndex++;
        }
        return result;
    }

    private ImagePart downloadImage(String url, int mediaIndex) {
        return mediaRestClient.get()
                .uri(url)
                .exchange((request, response) -> {
                    MediaType contentType = response.getHeaders().getContentType();
                    byte[] bytes = response.getBody().readAllBytes();
                    if (bytes.length == 0) {
                        throw new IllegalStateException("Downloaded media is empty");
                    }
                    String mimeType = normalizeMimeType(url, contentType);
                    return new ImagePart(mediaIndex, mimeType, Base64.getEncoder().encodeToString(bytes));
                });
    }

    String normalizeMimeType(String url, MediaType contentType) {
        if (contentType != null && isSupportedImageMimeType(contentType.toString())) {
            return contentType.toString();
        }

        String inferredMimeType = inferMimeTypeFromUrl(url);
        if (inferredMimeType != null) {
            return inferredMimeType;
        }

        if (contentType != null && MediaType.IMAGE_GIF_VALUE.equalsIgnoreCase(contentType.toString())) {
            return MediaType.IMAGE_PNG_VALUE;
        }

        throw new IllegalStateException("Unsupported media MIME type: "
                + (contentType == null ? "null" : contentType));
    }

    private boolean isSupportedImageMimeType(String mimeType) {
        return MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(mimeType)
                || MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(mimeType)
                || "image/webp".equalsIgnoreCase(mimeType);
    }

    private String inferMimeTypeFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        var matcher = FILE_EXTENSION_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }
        String extension = matcher.group(1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "webp" -> "image/webp";
            default -> null;
        };
    }

    private GeminiDraftResult requestGemini(
            String caption,
            List<String> hashTags,
            List<ImagePart> imageParts,
            List<Category> categories) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("models", model + ":generateContent")
                .queryParam("key", apiKey)
                .build(true)
                .toUri();

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", buildPrompt(caption, hashTags, imageParts, categories)));
        for (ImagePart imagePart : imageParts) {
            parts.add(Map.of("inlineData", Map.of(
                    "mimeType", imagePart.mimeType(),
                    "data", imagePart.base64Data())));
        }

        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", parts)),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "responseMimeType", "application/json"));

        try {
            JsonNode response = geminiRestClient.post()
                    .uri(uri)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            return parseResponse(response);
        } catch (RestClientException | JsonProcessingException e) {
            throw new IllegalStateException("Gemini draft analysis failed", e);
        }
    }

    GeminiDraftResult parseResponse(JsonNode response) throws JsonProcessingException {
        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini response text is empty");
        }

        JsonNode root = objectMapper.readTree(stripCodeFence(text));
        return new GeminiDraftResult(
                readStringList(root.get("hashTags")),
                readDraftPlaceCandidates(root.get("draftPlaces")),
                readStringList(root.get("warnings")));
    }

    private String buildPrompt(
            String caption,
            List<String> hashTags,
            List<ImagePart> imageParts,
            List<Category> categories) {
        String categoryLines = categories == null || categories.isEmpty()
                ? "- none"
                : categories.stream()
                        .filter(category -> category.getId() != null)
                        .map(category -> "- %d: %s".formatted(category.getId(), category.getName()))
                        .reduce((left, right) -> left + "\n" + right)
                        .orElse("- none");
        String mediaLines = imageParts.isEmpty()
                ? "- no images"
                : imageParts.stream()
                        .map(imagePart -> "- imageIndex=%d".formatted(imagePart.mediaIndex()))
                        .reduce((left, right) -> left + "\n" + right)
                        .orElse("- no images");
        return promptTemplateLoader.loadInstagramDraftPrompt(Map.of(
                "categories", categoryLines,
                "images", mediaLines,
                "caption", caption == null ? "" : caption,
                "hashtags", hashTags == null ? "[]" : hashTags.toString()));
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

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode element : node) {
            if (element != null && element.isTextual()) {
                String value = element.asText().trim();
                if (!value.isBlank()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private List<InstagramPreviewDto.DraftPlaceCandidate> readDraftPlaceCandidates(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<InstagramPreviewDto.DraftPlaceCandidate> values = new ArrayList<>();
        for (JsonNode element : node) {
            Integer imageIndex = element.hasNonNull("imageIndex") && element.get("imageIndex").canConvertToInt()
                    ? element.get("imageIndex").asInt()
                    : null;
            List<Long> categoryIds = new ArrayList<>();
            JsonNode categoryNode = element.get("categoryIds");
            if (categoryNode != null && categoryNode.isArray()) {
                for (JsonNode categoryIdNode : categoryNode) {
                    if (categoryIdNode != null && categoryIdNode.canConvertToLong()) {
                        categoryIds.add(categoryIdNode.asLong());
                    }
                }
            }
            values.add(InstagramPreviewDto.DraftPlaceCandidate.builder()
                    .imageIndex(imageIndex)
                    .description(readNullableText(element, "description"))
                    .categoryIds(categoryIds)
                    .build());
        }
        return values;
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

    private List<String> normalizeHashTags(List<String> aiHashTags, String caption, List<String> extractedHashTags) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        addHashTags(normalized, aiHashTags);
        addHashTags(normalized, extractedHashTags);
        if (normalized.isEmpty() && caption != null) {
            addHashTags(normalized, extractHashTagsFromCaption(caption));
        }
        return normalized.stream()
                .limit(MAX_HASHTAG_COUNT)
                .toList();
    }

    private void addHashTags(Set<String> sink, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            sink.add(trimmed.startsWith("#") ? trimmed : "#" + trimmed);
        }
    }

    private List<String> extractHashTagsFromCaption(String caption) {
        return List.of(caption.split("\\s+")).stream()
                .filter(token -> token.startsWith("#") && token.length() > 1)
                .toList();
    }

    private List<InstagramPreviewDto.DraftPlaceCandidate> normalizeCandidates(
            List<InstagramPreviewDto.DraftPlaceCandidate> candidates) {
        if (candidates == null) {
            return List.of();
        }
        return candidates.stream()
                .filter(Objects::nonNull)
                .map(candidate -> InstagramPreviewDto.DraftPlaceCandidate.builder()
                        .imageIndex(candidate.getImageIndex())
                        .description(limitLength(candidate.getDescription()))
                        .categoryIds(candidate.getCategoryIds() == null ? List.of() : candidate.getCategoryIds().stream()
                                .filter(Objects::nonNull)
                                .distinct()
                                .limit(2)
                                .toList())
                        .build())
                .toList();
    }

    private List<String> mergeWarnings(List<String> left, List<String> right) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        addWarnings(merged, left);
        addWarnings(merged, right);
        return List.copyOf(merged);
    }

    private void addWarnings(Set<String> sink, List<String> warnings) {
        if (warnings == null) {
            return;
        }
        for (String warning : warnings) {
            if (warning != null && !warning.isBlank()) {
                sink.add(warning);
            }
        }
    }

    private String limitLength(String value) {
        if (value == null || value.isBlank()) {
            return "분위기부터 저장하고 싶은 한 끼 스팟";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_DESCRIPTION_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_DESCRIPTION_LENGTH - 1).trim() + "…";
    }

    private record ImagePart(int mediaIndex, String mimeType, String base64Data) {
    }

    record GeminiDraftResult(
            List<String> hashTags,
            List<InstagramPreviewDto.DraftPlaceCandidate> draftPlaces,
            List<String> warnings) {
    }
}
