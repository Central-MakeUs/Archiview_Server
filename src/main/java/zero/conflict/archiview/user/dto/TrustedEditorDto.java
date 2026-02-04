package zero.conflict.archiview.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Hashtags;

import java.util.List;
import java.util.UUID;

@Schema(description = "믿고 먹는 에디터 DTO")
public class TrustedEditorDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "믿고 먹는 에디터 목록 응답")
    public static class ListResponse {
        private List<EditorResponse> editors;

        public static ListResponse from(List<EditorResponse> editors) {
            return ListResponse.builder()
                    .editors(editors)
                    .build();
        }

        public static ListResponse empty() {
            return ListResponse.builder()
                    .editors(List.of())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 프로필 정보")
    public static class EditorResponse {
        @Schema(description = "에디터 ID")
        private UUID editorId;
        @Schema(description = "에디터 닉네임", example = "맛집탐방가")
        private String nickname;
        @Schema(description = "인스타그램 ID", example = "editor_insta")
        private String instagramId;
        @Schema(description = "인스타그램 URL", example = "https://www.instagram.com/editor_insta")
        private String instagramUrl;
        @Schema(description = "한줄 소개", example = "서울의 숨은 맛집을 기록합니다.")
        private String introduction;
        @Schema(description = "해시태그 2개", example = "[\"#성수카페\", \"#디저트맛집\"]")
        private List<String> hashtags;
        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;

        public static EditorResponse from(EditorProfile profile) {
            Hashtags hashtags = profile.getHashtags();
            return EditorResponse.builder()
                    .editorId(profile.getUser().getId())
                    .nickname(profile.getNickname())
                    .instagramId(profile.getInstagramId())
                    .instagramUrl(profile.getInstagramUrl())
                    .introduction(profile.getIntroduction())
                    .hashtags(hashtags != null
                            ? List.of(hashtags.getPrimaryTag(), hashtags.getSecondaryTag())
                            : List.of())
                    .profileImageUrl(profile.getProfileImageUrl())
                    .build();
        }
    }
}
