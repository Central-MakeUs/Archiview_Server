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
        @Schema(description = "AI가 post 생성용으로 정리한 해시태그 최대 3개")
        private List<String> hashTags;
        @Schema(description = "AI가 장소별로 정리한 post 생성 초안 목록")
        private List<DraftPlace> draftPlaces;
        @Schema(description = "부분 성공 또는 보정 이유 목록")
        private List<String> warnings;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DraftPlace {
        @Schema(description = "장소 대표 사진 URL")
        private String imageUrl;
        @Schema(description = "50자 이내 맛집 소개 문체 설명")
        private String description;
        @Schema(description = "기존 카테고리 ID 목록, 1~2개")
        private List<Long> categoryIds;
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
    public static class DraftAnalysis {
        @Schema(description = "AI가 정리한 해시태그 최대 3개")
        private List<String> hashTags;
        @Schema(description = "AI가 정리한 장소 초안 목록")
        private List<DraftPlaceCandidate> draftPlaces;
        @Schema(description = "AI 분석 경고 목록")
        private List<String> warnings;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DraftPlaceCandidate {
        @Schema(description = "입력 미디어 목록 기준 0-based 대표 이미지 인덱스")
        private Integer imageIndex;
        @Schema(description = "50자 이내 맛집 소개 문체 설명")
        private String description;
        @Schema(description = "기존 카테고리 ID 목록, 1~2개")
        private List<Long> categoryIds;
    }

    public enum MediaType {
        IMAGE,
        VIDEO
    }
}
