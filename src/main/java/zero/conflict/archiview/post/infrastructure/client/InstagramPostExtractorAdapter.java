package zero.conflict.archiview.post.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.application.port.out.InstagramHtmlAiAnalyzer;
import zero.conflict.archiview.post.application.port.out.InstagramPostExtractor;
import zero.conflict.archiview.post.domain.InstagramUrl;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class InstagramPostExtractorAdapter implements InstagramPostExtractor {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(?<!\\S)#([\\p{L}\\p{N}_]+)");
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("https:\\\\/\\\\/[^\"\\\\]+");
    private static final int MAX_SCRIPT_BLOCKS = 5;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final InstagramHtmlAiAnalyzer instagramHtmlAiAnalyzer;
    private final int maxHtmlChars;

    public InstagramPostExtractorAdapter(
            InstagramHtmlAiAnalyzer instagramHtmlAiAnalyzer,
            ObjectMapper objectMapper,
            @Value("${post.instagram.user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36}") String userAgent,
            @Value("${instagram.html.max-chars:12000}") int maxHtmlChars) {
        this.instagramHtmlAiAnalyzer = instagramHtmlAiAnalyzer;
        this.objectMapper = objectMapper;
        this.maxHtmlChars = maxHtmlChars;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .build();
    }

    @Override
    public ExtractedInstagramPost extract(String instagramUrl) {
        String normalizedUrl = InstagramUrl.from(instagramUrl).getValue();
        boolean blockedOrPrivate = false;
        ExtractedInstagramPost merged = null;

        for (String fetchUrl : candidateUrls(normalizedUrl)) {
            String html = fetchHtml(fetchUrl);
            if (html == null || html.isBlank()) {
                continue;
            }
            if (isPrivateOrBlocked(html)) {
                blockedOrPrivate = true;
                continue;
            }

            ExtractedInstagramPost extracted = analyzeHtml(normalizedUrl, html);
            merged = mergeExtractedPosts(merged, extracted);
            if (isUsable(merged)) {
                return merged;
            }
        }

        merged = mergeExtractedPosts(merged, fetchFromOEmbed(normalizedUrl));
        if (isUsable(merged)) {
            return merged;
        }
        if (merged != null) {
            return merged;
        }
        if (blockedOrPrivate) {
            throw new DomainException(PostErrorCode.POST_INSTAGRAM_PRIVATE_OR_BLOCKED);
        }
        throw new DomainException(PostErrorCode.POST_INSTAGRAM_PREVIEW_UNAVAILABLE);
    }

    ExtractedInstagramPost analyzeHtml(String sourceUrl, String html) {
        if (html == null || html.isBlank()) {
            throw new DomainException(PostErrorCode.POST_INSTAGRAM_PREVIEW_UNAVAILABLE);
        }

        if (isPrivateOrBlocked(html)) {
            throw new DomainException(PostErrorCode.POST_INSTAGRAM_PRIVATE_OR_BLOCKED);
        }

        Document document = Jsoup.parse(html);
        String normalizedHtml = normalizeHtmlForAi(document);
        try {
            ExtractedInstagramPost aiResult = instagramHtmlAiAnalyzer.analyze(sourceUrl, normalizedHtml);
            if (isUsable(aiResult)) {
                return aiResult;
            }
            log.info("Gemini analysis returned unusable result. fallback parsing. url={}", sourceUrl);
        } catch (RuntimeException e) {
            log.info("Gemini analysis failed. fallback parsing. url={}", sourceUrl, e);
        }

        return parseWithFallback(sourceUrl, document, html);
    }

    private List<String> candidateUrls(String normalizedUrl) {
        String trimmed = normalizedUrl.endsWith("/") ? normalizedUrl.substring(0, normalizedUrl.length() - 1) : normalizedUrl;
        return List.of(trimmed + "/", trimmed + "/embed/captioned/");
    }

    private String fetchHtml(String url) {
        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT,
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header(HttpHeaders.REFERER, "https://www.instagram.com/")
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.warn("Instagram preview fetch failed. url={}", url, e);
            return null;
        }
    }

    private ExtractedInstagramPost fetchFromOEmbed(String normalizedUrl) {
        try {
            String json = restClient.get()
                    .uri(UriComponentsBuilder.fromUriString("https://www.instagram.com/api/v1/oembed/")
                            .queryParam("url", normalizedUrl)
                            .build(true)
                            .toUri())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(String.class);
            if (json == null || json.isBlank()) {
                return null;
            }
            return parseOEmbed(normalizedUrl, objectMapper.readTree(json));
        } catch (RestClientException | IOException e) {
            log.info("Instagram oEmbed fetch failed. url={}", normalizedUrl, e);
            return null;
        }
    }

    private boolean isUsable(ExtractedInstagramPost extracted) {
        if (extracted == null) {
            return false;
        }
        boolean hasCaption = extracted.caption() != null && !extracted.caption().isBlank();
        boolean hasMedia = extracted.mediaList() != null && !extracted.mediaList().isEmpty();
        return hasCaption || hasMedia;
    }

    private ExtractedInstagramPost parseWithFallback(String sourceUrl, Document document, String html) {
        String caption = firstNonBlank(
                extractMeta(document, "meta[property=og:description]"),
                extractCaptionFromJsonLd(document),
                extractCaptionFromOgTitle(document));

        List<String> hashTags = extractHashTags(caption);
        List<ExtractedMedia> mediaList = extractMedia(document, html);

        return new ExtractedInstagramPost(sourceUrl, blankToNull(caption), hashTags, mediaList);
    }

    ExtractedInstagramPost mergeExtractedPosts(ExtractedInstagramPost primary, ExtractedInstagramPost secondary) {
        if (primary == null) {
            return secondary;
        }
        if (secondary == null) {
            return primary;
        }

        String caption = firstNonBlank(primary.caption(), secondary.caption());
        Set<String> hashTags = new LinkedHashSet<>();
        if (primary.hashTags() != null) {
            hashTags.addAll(primary.hashTags());
        }
        if (secondary.hashTags() != null) {
            hashTags.addAll(secondary.hashTags());
        }

        Map<String, ExtractedMedia> mediaByUrl = new java.util.LinkedHashMap<>();
        mergeMedia(mediaByUrl, primary.mediaList());
        mergeMedia(mediaByUrl, secondary.mediaList());

        return new ExtractedInstagramPost(
                primary.sourceUrl() != null ? primary.sourceUrl() : secondary.sourceUrl(),
                blankToNull(caption),
                List.copyOf(hashTags).subList(0, Math.min(hashTags.size(), 3)),
                List.copyOf(mediaByUrl.values()));
    }

    private void mergeMedia(Map<String, ExtractedMedia> mediaByUrl, List<ExtractedMedia> mediaList) {
        if (mediaList == null) {
            return;
        }
        for (ExtractedMedia media : mediaList) {
            if (media == null || media.sourceUrl() == null || media.sourceUrl().isBlank()) {
                continue;
            }
            mediaByUrl.merge(media.sourceUrl(), media, (existing, incoming) ->
                    existing.mediaType() == InstagramPreviewDto.MediaType.VIDEO ? existing : incoming);
        }
    }

    ExtractedInstagramPost parseOEmbed(String sourceUrl, JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        String caption = readNullableText(root, "title");
        String thumbnailUrl = readNullableText(root, "thumbnail_url");
        String html = readNullableText(root, "html");
        boolean isVideo = isVideoOEmbed(sourceUrl, html);

        List<ExtractedMedia> mediaList = thumbnailUrl == null
                ? List.of()
                : List.of(new ExtractedMedia(
                        thumbnailUrl,
                        isVideo ? InstagramPreviewDto.MediaType.VIDEO : InstagramPreviewDto.MediaType.IMAGE));

        return new ExtractedInstagramPost(
                sourceUrl,
                caption,
                extractHashTags(caption),
                mediaList);
    }

    private boolean isVideoOEmbed(String sourceUrl, String html) {
        String loweredSourceUrl = sourceUrl.toLowerCase(Locale.ROOT);
        if (loweredSourceUrl.contains("/reel/")) {
            return true;
        }
        if (html == null) {
            return false;
        }
        String loweredHtml = html.toLowerCase(Locale.ROOT);
        return loweredHtml.contains("/reel/")
                || loweredHtml.contains("instagram reel")
                || loweredHtml.contains("data-instgrm-permalink=\"https://www.instagram.com/reel/");
    }

    String normalizeHtmlForAi(Document originalDocument) {
        Document document = originalDocument.clone();
        document.select("style, noscript, svg").remove();

        StringBuilder builder = new StringBuilder();
        appendTitle(document, builder);
        appendRelevantMeta(document, builder);
        appendJsonLd(document, builder);
        appendRelevantScripts(document, builder);
        appendBodyText(document, builder);

        String normalized = builder.toString().trim();
        if (normalized.length() <= maxHtmlChars) {
            return normalized;
        }
        return normalized.substring(0, maxHtmlChars);
    }

    private void appendTitle(Document document, StringBuilder builder) {
        String title = blankToNull(document.title());
        if (title != null) {
            builder.append("<title>").append(title).append("</title>\n");
        }
    }

    private void appendRelevantMeta(Document document, StringBuilder builder) {
        Elements metaElements = document.select(
                "meta[property^=og:], meta[name^=twitter:], meta[name=description], meta[property=description]");
        for (Element meta : metaElements) {
            String key = firstNonBlank(meta.attr("property"), meta.attr("name"));
            String value = blankToNull(meta.attr("content"));
            if (key != null && value != null) {
                builder.append("<meta key=\"").append(key).append("\">")
                        .append(value)
                        .append("</meta>\n");
            }
        }
    }

    private void appendJsonLd(Document document, StringBuilder builder) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            String data = blankToNull(script.data());
            if (data != null) {
                builder.append("<jsonld>").append(truncate(data, maxHtmlChars / 3)).append("</jsonld>\n");
            }
        }
    }

    private void appendRelevantScripts(Document document, StringBuilder builder) {
        int appended = 0;
        for (Element script : document.select("script")) {
            if (appended >= MAX_SCRIPT_BLOCKS) {
                break;
            }
            String data = blankToNull(script.data());
            if (data == null || !containsInstagramSignal(data)) {
                continue;
            }
            builder.append("<script-snippet>")
                    .append(truncate(data, 1200))
                    .append("</script-snippet>\n");
            appended++;
        }
    }

    private void appendBodyText(Document document, StringBuilder builder) {
        for (Element body : document.select("body")) {
            removeScriptsAndStyles(body);
            String text = blankToNull(body.text());
            if (text != null) {
                builder.append("<body-text>").append(truncate(text, maxHtmlChars / 2)).append("</body-text>\n");
            }
            break;
        }
    }

    private void removeScriptsAndStyles(Element body) {
        for (Node node : body.childNodes()) {
            if (node instanceof Element element) {
                if ("script".equalsIgnoreCase(element.tagName()) || "style".equalsIgnoreCase(element.tagName())) {
                    element.remove();
                    continue;
                }
                removeScriptsAndStyles(element);
            }
        }
    }

    private boolean containsInstagramSignal(String data) {
        String lowered = data.toLowerCase(Locale.ROOT);
        return lowered.contains("display_url")
                || lowered.contains("thumbnail_src")
                || lowered.contains("video_url")
                || lowered.contains("caption")
                || lowered.contains("accessibility_caption");
    }

    private String truncate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit);
    }

    private boolean isPrivateOrBlocked(String html) {
        String lowered = html.toLowerCase(Locale.ROOT);
        return lowered.contains("page isn&#x27;t available")
                || lowered.contains("page isn't available")
                || lowered.contains("accounts/login")
                || lowered.contains("로그인");
    }

    private String extractMeta(Document document, String selector) {
        Element element = document.selectFirst(selector);
        return element == null ? null : blankToNull(element.attr("content"));
    }

    private String extractCaptionFromOgTitle(Document document) {
        String ogTitle = extractMeta(document, "meta[property=og:title]");
        if (ogTitle == null) {
            return null;
        }
        int firstQuote = ogTitle.indexOf('“');
        int lastQuote = ogTitle.lastIndexOf('”');
        if (firstQuote >= 0 && lastQuote > firstQuote) {
            return blankToNull(ogTitle.substring(firstQuote + 1, lastQuote));
        }
        return null;
    }

    private String extractCaptionFromJsonLd(Document document) {
        for (Element element : document.select("script[type=application/ld+json]")) {
            String json = element.data();
            if (json == null || json.isBlank()) {
                continue;
            }
            try {
                JsonNode root = objectMapper.readTree(json);
                String caption = findFirstText(root, "caption", "articleBody", "description", "name");
                if (caption != null) {
                    return caption;
                }
            } catch (IOException e) {
                log.debug("Failed to parse Instagram json-ld", e);
            }
        }
        return null;
    }

    private String findFirstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode child = node.get(fieldName);
            if (child != null && child.isTextual() && !child.asText().isBlank()) {
                return child.asText();
            }
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                String found = findFirstText(child, fieldNames);
                if (found != null) {
                    return found;
                }
            }
        }
        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                String found = findFirstText(fields.next().getValue(), fieldNames);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private List<String> extractHashTags(String caption) {
        if (caption == null || caption.isBlank()) {
            return List.of();
        }
        Set<String> hashTags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(caption);
        while (matcher.find() && hashTags.size() < 3) {
            hashTags.add("#" + matcher.group(1));
        }
        return List.copyOf(hashTags);
    }

    private List<ExtractedMedia> extractMedia(Document document, String html) {
        Set<String> imageUrls = new LinkedHashSet<>();
        addIfPresent(imageUrls, extractMeta(document, "meta[property=og:image]"));
        addIfPresent(imageUrls, extractMeta(document, "meta[property=og:image:secure_url]"));

        for (Element element : document.select("script[type=application/ld+json]")) {
            String json = element.data();
            if (json == null || json.isBlank()) {
                continue;
            }
            try {
                JsonNode root = objectMapper.readTree(json);
                collectImageUrls(root, imageUrls);
            } catch (IOException e) {
                log.debug("Failed to collect image urls from json-ld", e);
            }
        }

        Matcher matcher = MEDIA_URL_PATTERN.matcher(html);
        while (matcher.find() && imageUrls.size() < 10) {
            String candidate = matcher.group()
                    .replace("\\u0026", "&")
                    .replace("\\/", "/");
            if (candidate.contains("cdninstagram.com") || candidate.contains("fbcdn.net")) {
                imageUrls.add(candidate);
            }
        }

        boolean hasVideo = hasVideoSignal(document, html);
        List<ExtractedMedia> mediaList = new ArrayList<>();
        boolean first = true;
        for (String imageUrl : imageUrls) {
            mediaList.add(new ExtractedMedia(
                    imageUrl,
                    hasVideo && first ? InstagramPreviewDto.MediaType.VIDEO : InstagramPreviewDto.MediaType.IMAGE));
            first = false;
        }
        return mediaList;
    }

    private boolean hasVideoSignal(Document document, String html) {
        if (document.selectFirst("meta[property=og:video]") != null) {
            return true;
        }
        String ogUrl = extractMeta(document, "meta[property=og:url]");
        if (ogUrl != null && ogUrl.contains("/reel/")) {
            return true;
        }
        String twitterTitle = extractMeta(document, "meta[name=twitter:title]");
        if (twitterTitle != null && twitterTitle.toLowerCase(Locale.ROOT).contains("instagram reel")) {
            return true;
        }
        String loweredHtml = html.toLowerCase(Locale.ROOT);
        return loweredHtml.contains("data-media-type=\"graphvideo\"")
                || loweredHtml.contains("\"graphvideo\"");
    }

    private void collectImageUrls(JsonNode node, Set<String> imageUrls) {
        if (node == null || node.isNull() || imageUrls.size() >= 10) {
            return;
        }
        if (node.isTextual()) {
            String value = node.asText();
            if (value.startsWith("https://") && (value.contains("cdninstagram.com") || value.contains("fbcdn.net"))) {
                imageUrls.add(value);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectImageUrls(child, imageUrls);
            }
            return;
        }
        if (node.has("image")) {
            collectImageUrls(node.get("image"), imageUrls);
        }
        var fields = node.fields();
        while (fields.hasNext()) {
            collectImageUrls(fields.next().getValue(), imageUrls);
        }
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

    private void addIfPresent(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
