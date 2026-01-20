package zero.conflict.archiview.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        @Schema(description = "한줄 소개", example = "서울의 숨은 맛집을 기록합니다.")
        private String bio;
        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 프로필 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "수정할 닉네임", example = "새로운닉네임")
        private String nickname;
        @Schema(description = "수정할 인스타그램 ID", example = "new_insta")
        private String instagramId;
        @Schema(description = "수정할 한줄 소개", example = "한줄 소개를 수정합니다.")
        private String bio;
        @Schema(description = "수정할 프로필 이미지 URL")
        private String profileImageUrl;
    }
}
