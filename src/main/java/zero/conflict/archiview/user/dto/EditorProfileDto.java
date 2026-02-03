package zero.conflict.archiview.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.user.domain.EditorProfile;

public class EditorProfileDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 프로필 정보 응답")
    public static class Response {
        @Schema(description = "에디터 닉네임", example = "맛집탐방가")
        private String nickname;
        @Schema(description = "인스타그램 ID", example = "editor_insta")
        private String instagramId;
        @Schema(description = "인스타그램 URL", example = "https://www.instagram.com/editor_insta")
        private String instagramUrl;
        @Schema(description = "한줄 소개", example = "서울의 숨은 맛집을 기록합니다.")
        private String introduction;
        @Schema(description = "해시태그 2개", example = "[\"#성수카페\", \"#디저트맛집\"]")
        private java.util.List<String> hashtags;
        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;

        public static Response from(EditorProfile profile) {
            return Response.builder()
                    .nickname(profile.getNickname())
                    .instagramId(profile.getInstagramId())
                    .instagramUrl(profile.getInstagramUrl())
                    .introduction(profile.getIntroduction())
                    .hashtags(java.util.List.of(
                            profile.getHashtags().getPrimaryTag(),
                            profile.getHashtags().getSecondaryTag()))
                    .profileImageUrl(profile.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "인스타그램 ID 중복 확인 응답")
    public static class InstagramIdCheckResponse {
        @Schema(description = "이미 사용 중인지 여부", example = "true")
        private boolean exists;

        public static InstagramIdCheckResponse of(boolean exists) {
            return InstagramIdCheckResponse.builder()
                    .exists(exists)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 프로필 등록 요청")
    public static class CreateRequest {
        @jakarta.validation.constraints.NotBlank
        @Schema(description = "에디터 닉네임", example = "맛집탐방가")
        private String nickname;
        @jakarta.validation.constraints.NotBlank
        @Schema(description = "인스타그램 ID", example = "editor_insta")
        private String instagramId;
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Pattern(
                regexp = "^https://(www\\.)?instagram\\.com/[^\\s]+$",
                message = "유효한 인스타그램 URL 형식이어야 합니다.")
        @Schema(description = "인스타그램 URL", example = "https://www.instagram.com/editor_insta")
        private String instagramUrl;
        @jakarta.validation.constraints.NotBlank
        @Schema(description = "한줄 소개", example = "서울의 숨은 맛집을 기록합니다.")
        private String introduction;
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Size(min = 2, max = 2)
        @Schema(description = "해시태그 2개", example = "[\"#성수카페\", \"#디저트맛집\"]")
        private java.util.List<@jakarta.validation.constraints.NotBlank String> hashtags;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 프로필 수정 요청")
    public static class UpdateRequest {
        @jakarta.validation.constraints.NotBlank
        @Schema(description = "수정할 닉네임", example = "새로운닉네임")
        private String nickname;
        @jakarta.validation.constraints.NotBlank
        @Schema(description = "수정할 인스타그램 ID", example = "new_insta")
        private String instagramId;
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Pattern(
                regexp = "^https://(www\\.)?instagram\\.com/[^\\s]+$",
                message = "유효한 인스타그램 URL 형식이어야 합니다.")
        @Schema(description = "수정할 인스타그램 URL", example = "https://www.instagram.com/new_insta")
        private String instagramUrl;
        @jakarta.validation.constraints.NotBlank
        @Schema(description = "수정할 한줄 소개", example = "한줄 소개를 수정합니다.")
        private String introduction;
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Size(min = 2, max = 2)
        @Schema(description = "수정할 해시태그 2개", example = "[\"#성수카페\", \"#디저트맛집\"]")
        private java.util.List<@jakarta.validation.constraints.NotBlank String> hashtags;
        @Schema(description = "수정할 프로필 이미지 URL")
        private String profileImageUrl;
    }
}
