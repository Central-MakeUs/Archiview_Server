package zero.conflict.archiview.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ArchiverPlaceCommandDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstagramInflowCountResponse {
        private Long postPlaceId;
        private Long instagramInflowCount;

        public static InstagramInflowCountResponse of(Long postPlaceId, Long instagramInflowCount) {
            return InstagramInflowCountResponse.builder()
                    .postPlaceId(postPlaceId)
                    .instagramInflowCount(instagramInflowCount)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DirectionCountResponse {
        private Long postPlaceId;
        private Long directionCount;

        public static DirectionCountResponse of(Long postPlaceId, Long directionCount) {
            return DirectionCountResponse.builder()
                    .postPlaceId(postPlaceId)
                    .directionCount(directionCount)
                    .build();
        }
    }
}
