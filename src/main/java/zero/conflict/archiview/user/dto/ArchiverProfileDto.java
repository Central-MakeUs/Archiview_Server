package zero.conflict.archiview.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "아카이버 프로필 관련 DTO")
public class ArchiverProfileDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "아카이버 프로필 정보 응답")
    public static class Response {
        @Schema(description = "아카이버 닉네임", example = "장소수집가")
        private String nickname;
        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "아카이버 프로필 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "수정할 닉네임", example = "새닉네임")
        private String nickname;
        @Schema(description = "수정할 프로필 이미지 URL")
        private String profileImageUrl;
    }
}
