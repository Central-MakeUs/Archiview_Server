package zero.conflict.archiview.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class InstagramPreviewDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "InstagramPreviewRequest")
    public static class Request {
        @NotBlank(message = "URL은 필수입니다.")
        @Schema(description = "인스타그램 게시글 URL", example = "https://www.instagram.com/p/DBU0yXOz_A-/")
        private String url;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "InstagramPreviewResponse")
    public static class Response {
        @Schema(description = "정규화된 인스타그램 URL")
        private String sourceUrl;
        @Schema(description = "게시글 캡션")
        private String caption;
        @Schema(description = "자동 추출된 해시태그 최대 3개")
        private List<String> hashTags;
        @Schema(description = "대표 이미지 URL")
        private String primaryImageUrl;
        @Schema(description = "S3에 저장된 전체 이미지 URL 목록")
        private List<String> allImageUrls;
        @Schema(description = "추출된 미디어 목록")
        private List<MediaItem> mediaList;
        @Schema(description = "추출 상태", example = "PARTIAL_SUCCESS")
        private ExtractStatus extractStatus;
        @Schema(description = "누락된 필드 목록")
        private List<String> missingFields;
        @Schema(description = "부분 성공 또는 스킵 이유 목록")
        private List<String> warnings;
        @Schema(description = "캡션/미디어 기반 상세 분석 결과")
        private ContentAnalysis contentAnalysis;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaItem {
        @Schema(description = "원본 인스타 미디어 URL")
        private String sourceUrl;
        @Schema(description = "S3에 저장된 미디어 URL")
        private String storedUrl;
        @Schema(description = "미디어 타입", example = "IMAGE")
        private MediaType mediaType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentAnalysis {
        @Schema(description = "분석 상태", example = "PARTIAL_SUCCESS")
        private AnalysisStatus status;
        @Schema(description = "작성자 캡션 요약")
        private String captionSummary;
        @Schema(description = "이미지/썸네일에서 읽은 텍스트")
        private String visibleText;
        @Schema(description = "이미지/썸네일 장면 설명")
        private String sceneDescription;
        @Schema(description = "음성 전사 텍스트")
        private String audioTranscript;
        @Schema(description = "분석 경고 목록")
        private List<String> warnings;
    }

    public enum ExtractStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED
    }

    public enum AnalysisStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        SKIPPED,
        FAILED
    }

    public enum MediaType {
        IMAGE,
        VIDEO
    }
}
