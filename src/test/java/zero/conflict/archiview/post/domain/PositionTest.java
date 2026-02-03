package zero.conflict.archiview.post.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Position 도메인 테스트")
class PositionTest {

    @Test
    @DisplayName("Position을 정상적으로 생성할 수 있다")
    void createPosition_success() {
        // when
        Position position = Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"));

        // then
        assertThat(position.getLatitude()).isEqualTo(Double.valueOf("37.5665"));
        assertThat(position.getLongitude()).isEqualTo(Double.valueOf("126.9780"));
    }

    @Test
    @DisplayName("위도가 범위를 벗어나면 에러코드를 반환한다")
    void createPosition_withInvalidLatitude_throwsDomainException() {
        // when & then
        assertThatThrownBy(() -> Position.of(Double.valueOf("90.0001"), Double.valueOf("0")))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_POSITION_LATITUDE);
            });

        assertThatThrownBy(() -> Position.of(Double.valueOf("-90.0001"), Double.valueOf("0")))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_POSITION_LATITUDE);
            });
    }

    @Test
    @DisplayName("경도가 범위를 벗어나면 에러코드를 반환한다")
    void createPosition_withInvalidLongitude_throwsDomainException() {
        // when & then
        assertThatThrownBy(() -> Position.of(Double.valueOf("0"), Double.valueOf("180.0001")))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_POSITION_LONGITUDE);
            });

        assertThatThrownBy(() -> Position.of(Double.valueOf("0"), Double.valueOf("-180.0001")))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_POSITION_LONGITUDE);
            });
    }
}
