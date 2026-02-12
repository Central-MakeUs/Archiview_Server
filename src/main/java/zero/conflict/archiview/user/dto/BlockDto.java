package zero.conflict.archiview.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Hashtags;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BlockDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "BlockedEditorListResponse", description = "내 차단 에디터 목록 응답")
    public static class ListResponse {
        @Schema(description = "차단한 에디터 수")
        private int totalCount;
        private List<BlockedEditorResponse> editors;

        public static ListResponse from(List<BlockedEditorResponse> editors) {
            return ListResponse.builder()
                    .totalCount(editors.size())
                    .editors(editors)
                    .build();
        }

        public static ListResponse empty() {
            return ListResponse.builder()
                    .totalCount(0)
                    .editors(List.of())
                    .build();
        }

        public static ListResponse mock() {
            return ListResponse.builder()
                    .totalCount(2)
                    .editors(List.of(
                            BlockedEditorResponse.builder()
                                    .editorId(UUID.fromString("00000000-0000-0000-0000-000000000301"))
                                    .nickname("차단된에디터1")
                                    .instagramId("blocked_editor_1")
                                    .instagramUrl("https://www.instagram.com/blocked_editor_1")
                                    .introduction("소개1")
                                    .hashtags(List.of("#한식", "#맛집"))
                                    .profileImageUrl("https://picsum.photos/200/200?random=71")
                                    .blockedAt(LocalDateTime.now().minusDays(1))
                                    .build(),
                            BlockedEditorResponse.builder()
                                    .editorId(UUID.fromString("00000000-0000-0000-0000-000000000302"))
                                    .nickname("차단된에디터2")
                                    .instagramId("blocked_editor_2")
                                    .instagramUrl("https://www.instagram.com/blocked_editor_2")
                                    .introduction("소개2")
                                    .hashtags(List.of("#카페", "#디저트"))
                                    .profileImageUrl("https://picsum.photos/200/200?random=72")
                                    .blockedAt(LocalDateTime.now().minusDays(3))
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "차단된 에디터 정보")
    public static class BlockedEditorResponse {
        @Schema(description = "에디터 ID")
        private UUID editorId;
        @Schema(description = "에디터 닉네임")
        private String nickname;
        @Schema(description = "인스타그램 ID")
        private String instagramId;
        @Schema(description = "인스타그램 URL")
        private String instagramUrl;
        @Schema(description = "한줄 소개")
        private String introduction;
        @Schema(description = "해시태그 2개")
        private List<String> hashtags;
        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "차단 시각")
        private LocalDateTime blockedAt;

        public static BlockedEditorResponse from(EditorProfile profile, LocalDateTime blockedAt) {
            Hashtags hashtags = profile.getHashtags();
            return BlockedEditorResponse.builder()
                    .editorId(profile.getUser().getId())
                    .nickname(profile.getNickname())
                    .instagramId(profile.getInstagramId())
                    .instagramUrl(profile.getInstagramUrl())
                    .introduction(profile.getIntroduction())
                    .hashtags(hashtags != null
                            ? List.of(hashtags.getPrimaryTag(), hashtags.getSecondaryTag())
                            : List.of())
                    .profileImageUrl(profile.getProfileImageUrl())
                    .blockedAt(blockedAt)
                    .build();
        }
    }
}
