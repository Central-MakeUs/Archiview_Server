package zero.conflict.archiview.post.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.global.infra.s3.S3Service;
import zero.conflict.archiview.post.application.port.out.InstagramMediaStorage;
import zero.conflict.archiview.post.application.port.out.InstagramPostExtractor;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.io.ByteArrayInputStream;
import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstagramMediaStorageAdapter implements InstagramMediaStorage {

    private final S3Service s3Service;
    private final RestClient restClient = RestClient.create();

    @Override
    public StoredMedia store(InstagramPostExtractor.ExtractedMedia media) {
        try {
            byte[] bytes = restClient.get()
                    .uri(media.sourceUrl())
                    .retrieve()
                    .body(byte[].class);

            if (bytes == null || bytes.length == 0) {
                throw new DomainException(PostErrorCode.POST_INSTAGRAM_MEDIA_DOWNLOAD_FAILED);
            }

            String storedUrl = s3Service.upload(
                    new ByteArrayInputStream(bytes),
                    "posts",
                    extractFilename(media.sourceUrl()));
            return new StoredMedia(media.sourceUrl(), storedUrl, media.mediaType());
        } catch (RestClientException e) {
            log.warn("Instagram media download failed. url={}", media.sourceUrl(), e);
            throw new DomainException(PostErrorCode.POST_INSTAGRAM_MEDIA_DOWNLOAD_FAILED);
        }
    }

    private String extractFilename(String sourceUrl) {
        String path = URI.create(sourceUrl).getPath();
        int lastSlash = path == null ? -1 : path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return "instagram-media.jpg";
    }
}
