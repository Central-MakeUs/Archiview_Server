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

public class FollowDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "FollowCreateRequest", description = "팔로우 등록 요청")
    public static class CreateRequest {
        @jakarta.validation.constraints.NotNull
        @Schema(description = "에디터 ID")
        private UUID editorId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "FollowListResponse", description = "내 팔로우 목록 응답")
    public static class ListResponse {
        @Schema(description = "팔로우한 에디터 수")
        private int totalCount;
        private List<FollowingResponse> editors;

        public static ListResponse from(List<FollowingResponse> editors) {
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
                            FollowingResponse.builder()
                                    .editorId(UUID.fromString("00000000-0000-0000-0000-000000000201"))
                                    .nickname("맛집탐방가")
                                    .instagramId("editor_insta")
                                    .instagramUrl("https://www.instagram.com/editor_insta")
                                    .introduction("서울의 숨은 맛집을 기록합니다.")
                                    .hashtags(List.of("#성수카페", "#디저트맛집"))
                                    .profileImageUrl("https://picsum.photos/200/200?random=31")
                                    .build(),
                            FollowingResponse.builder()
                                    .editorId(UUID.fromString("00000000-0000-0000-0000-000000000202"))
                                    .nickname("연남동러버")
                                    .instagramId("yonnam_editor")
                                    .instagramUrl("https://www.instagram.com/yonnam_editor")
                                    .introduction("연남동 로컬 핫플 위주로 기록해요.")
                                    .hashtags(List.of("#연남동", "#로컬맛집"))
                                    .profileImageUrl("https://picsum.photos/200/200?random=32")
                                    .build()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "팔로잉 정보")
    public static class FollowingResponse {
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

        public static FollowingResponse from(EditorProfile profile) {
            Hashtags hashtags = profile.getHashtags();
            return FollowingResponse.builder()
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
