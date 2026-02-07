package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PresignedUrlCommandDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PresignedUrlRequest")
    public static class Request {
        @NotBlank(message = "파일명은 필수입니다.")
        @Schema(description = "원본 파일명", example = "photo.png")
        private String filename;

        @NotBlank(message = "파일 타입은 필수입니다.")
        @Schema(description = "파일 MIME 타입", example = "image/png")
        private String contentType;

        @NotNull(message = "파일 크기는 필수입니다.")
        @Schema(description = "파일 크기 (바이트)", example = "345678")
        private Long size;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PresignedUrlResponse")
    public static class Response {
        @Schema(description = "업로드용 presigned URL")
        private String uploadUrl;

        @Schema(description = "업로드 완료 후 최종 이미지 URL")
        private String imageUrl;

        @Schema(description = "S3 객체 키 (DB 저장용)")
        private String imageKey;

        public static Response of(String uploadUrl, String imageUrl, String imageKey) {
            return Response.builder()
                    .uploadUrl(uploadUrl)
                    .imageUrl(imageUrl)
                    .imageKey(imageKey)
                    .build();
        }
    }
}
