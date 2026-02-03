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
        @io.swagger.v3.oas.annotations.media.Schema(description = "원본 파일명", example = "photo.png")
        private String filename;

        @NotBlank(message = "파일 타입은 필수입니다.")
        @io.swagger.v3.oas.annotations.media.Schema(description = "파일 MIME 타입", example = "image/png")
        private String contentType;

        @NotNull(message = "파일 크기는 필수입니다.")
        @io.swagger.v3.oas.annotations.media.Schema(description = "파일 크기 (바이트)", example = "345678")
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

        public static Response of(String uploadUrl) {
            return Response.builder()
                    .uploadUrl(uploadUrl)
                    .build();
        }
    }
}
