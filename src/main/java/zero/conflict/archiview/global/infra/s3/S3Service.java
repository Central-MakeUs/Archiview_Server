package zero.conflict.archiview.global.infra.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Template s3Template;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일을 S3에 업로드하고 URL을 반환합니다.
     * 
     * @param file    업로드할 파일
     * @param dirName 저장할 디렉토리 이름
     * @return 업로드된 파일의 S3 URL
     */
    public String upload(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucket, fileName, inputStream);

            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            String url = s3Client.utilities().getUrl(getUrlRequest).toString();

            log.info("File uploaded to S3: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }
}
