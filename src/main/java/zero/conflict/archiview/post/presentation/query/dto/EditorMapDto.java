package zero.conflict.archiview.post.presentation.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;

import java.util.List;

public class EditorMapDto {

    public enum MapFilter {
        ALL, NEARBY, CATEGORY
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 지도 장소 핀 응답")
    public static class PlacePinResponse {
        @Schema(description = "장소 ID")
        private Long placeId;
        @Schema(description = "장소명")
        private String name;
        @Schema(description = "위도")
        private Double latitude;
        @Schema(description = "경도")
        private Double longitude;
        @Schema(description = "장소 카테고리 목록")
        private List<String> categories;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "에디터 지도 장소 목록 응답")
    public static class Response {
        @Schema(description = "필터링된 장소 핀 목록")
        private List<PlacePinResponse> pins;
    }
}
