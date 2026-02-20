package zero.conflict.archiview.global.infra.s3;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Template s3Template;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "presignExpirationSeconds", 300L);
    }

    @Test
    @DisplayName("Presigned URL 생성 시 파일 확장자가 .webp로 변환된다")
    void generatePresignedUploadUrl_convertsExtensionToWebp() throws Exception {
        // given
        String dirName = "posts";
        String originalFilename = "photo.png";
        String contentType = "image/png";

        PresignedPutObjectRequest presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
        given(presignedPutObjectRequest.url())
                .willReturn(new URL("https://test-bucket.s3.amazonaws.com/posts/uuid_photo.webp"));
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presignedPutObjectRequest);

        // when
        PresignedUrlInfo result = s3Service.generatePresignedUploadUrl(dirName, originalFilename, contentType);

        // then
        assertThat(result.key()).endsWith(".webp");
        assertThat(result.key()).startsWith("posts/");
        assertThat(result.key()).contains("photo");
        assertThat(result.key()).doesNotContain(".png");
    }

    @Test
    @DisplayName("확장자가 없는 파일명도 .webp로 변환된다")
    void generatePresignedUploadUrl_noExtension_addsWebp() throws Exception {
        // given
        String dirName = "profiles";
        String originalFilename = "avatar";
        String contentType = "image/jpeg";

        PresignedPutObjectRequest presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
        given(presignedPutObjectRequest.url())
                .willReturn(new URL("https://test-bucket.s3.amazonaws.com/profiles/uuid_avatar.webp"));
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presignedPutObjectRequest);

        // when
        PresignedUrlInfo result = s3Service.generatePresignedUploadUrl(dirName, originalFilename, contentType);

        // then
        assertThat(result.key()).endsWith("avatar.webp");
    }

    @Test
    @DisplayName("originalFilename이 null인 경우 file.webp로 생성된다")
    void generatePresignedUploadUrl_nullFilename_usesDefault() throws Exception {
        // given
        String dirName = "posts";
        String contentType = "image/png";

        PresignedPutObjectRequest presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
        given(presignedPutObjectRequest.url())
                .willReturn(new URL("https://test-bucket.s3.amazonaws.com/posts/uuid_file.webp"));
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presignedPutObjectRequest);

        // when
        PresignedUrlInfo result = s3Service.generatePresignedUploadUrl(dirName, null, contentType);

        // then
        assertThat(result.key()).endsWith("file.webp");
    }
}
