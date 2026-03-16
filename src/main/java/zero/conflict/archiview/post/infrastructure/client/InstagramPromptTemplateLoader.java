package zero.conflict.archiview.post.infrastructure.client;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class InstagramPromptTemplateLoader {

    private static final String INSTAGRAM_DRAFT_PROMPT_PATH = "prompts/instagram-draft-prompt.txt";

    public String loadInstagramDraftPrompt(Map<String, String> variables) {
        String template = readTemplate(INSTAGRAM_DRAFT_PROMPT_PATH);
        String resolved = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            resolved = resolved.replace(placeholder, entry.getValue() == null ? "" : entry.getValue());
        }
        return resolved;
    }

    private String readTemplate(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read prompt template: " + path, e);
        }
    }
}
