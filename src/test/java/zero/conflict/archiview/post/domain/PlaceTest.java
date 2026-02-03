package zero.conflict.archiview.post.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import zero.conflict.archiview.global.error.DomainException;
import error.PostErrorCode;

@DisplayName("Place 도메인 테스트")
class PlaceTest {

    @Test
    @DisplayName("Place를 정상적으로 생성할 수 있다")
    void createPlace_success() {
        // given
        String name = "테스트 장소";
        Address address = Address.of("서울시 강남구", "101호", "12345");
        Position position = Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"));
        String nearestStationWalkTime = "도보 5분";

        // when
        Place place = Place.createOf(name, address, position, nearestStationWalkTime);

        // then
        assertThat(place.getName()).isEqualTo(name);
        assertThat(place.getAddress()).isEqualTo(address);
        assertThat(place.getPosition()).isEqualTo(position);
        assertThat(place.getNearestStationWalkTime()).isEqualTo(nearestStationWalkTime);
    }

    @Test
    @DisplayName("같은 Position을 가진 Place는 동일한 위치로 간주된다")
    void samePosition_equals() {
        // given
        Position position = Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"));

        Place place1 = Place.createOf(
            "장소1",
            Address.of("주소1", "상세1", "11111"),
            position
        );

        Place place2 = Place.createOf(
            "장소2",
            Address.of("주소2", "상세2", "22222"),
            position
        );

        // then
        assertThat(place1.getPosition()).isEqualTo(place2.getPosition());
    }

    @Test
    @DisplayName("다른 Position을 가진 Place는 다른 위치로 간주된다")
    void differentPosition_notEquals() {
        // given
        Position position1 = Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"));
        Position position2 = Position.of(Double.valueOf("37.5700"), Double.valueOf("126.9800"));

        Place place1 = Place.createOf(
            "장소1",
            Address.of("주소1", "상세1", "11111"),
            position1
        );

        Place place2 = Place.createOf(
            "장소2",
            Address.of("주소2", "상세2", "22222"),
            position2
        );

        // then
        assertThat(place1.getPosition()).isNotEqualTo(place2.getPosition());
    }

    @Test
    @DisplayName("Place의 정보를 수정할 수 있다")
    void updatePlace_success() {
        // given
        Place place = Place.createOf(
            "원래 장소",
            Address.of("원래 주소", "상세1", "11111"),
            Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"))
        );

        String newName = "수정된 장소";
        Address newAddress = Address.of("수정된 주소", "상세2", "22222");

        // when
        place.update(newName, newAddress);

        // then
        assertThat(place.getName()).isEqualTo(newName);
        assertThat(place.getAddress()).isEqualTo(newAddress);
    }

    @Test
    @DisplayName("장소 이름이 null이면 에러코드를 반환한다")
    void updatePlace_withNullName_throwsDomainException() {
        // given
        Place place = Place.createOf(
            "원래 장소",
            Address.of("원래 주소", "상세1", "11111"),
            Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"))
        );
        Address newAddress = Address.of("수정된 주소", "상세2", "22222");

        // when & then
        assertThatThrownBy(() -> place.update(null, newAddress))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_PLACE_NAME);
            });
    }

    @Test
    @DisplayName("장소 이름이 빈 문자열이면 에러코드를 반환한다")
    void updatePlace_withEmptyName_throwsDomainException() {
        // given
        Place place = Place.createOf(
            "원래 장소",
            Address.of("원래 주소", "상세1", "11111"),
            Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"))
        );
        Address newAddress = Address.of("수정된 주소", "상세2", "22222");

        // when & then
        assertThatThrownBy(() -> place.update("", newAddress))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_PLACE_NAME);
            });
    }

    @Test
    @DisplayName("주소가 null이면 에러코드를 반환한다")
    void updatePlace_withNullAddress_throwsDomainException() {
        // given
        Place place = Place.createOf(
            "원래 장소",
            Address.of("원래 주소", "상세1", "11111"),
            Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"))
        );

        // when & then
        assertThatThrownBy(() -> place.update("수정된 장소", null))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> {
                DomainException domainException = (DomainException) ex;
                assertThat(domainException.getErrorCode()).isEqualTo(PostErrorCode.INVALID_PLACE_ADDRESS);
            });
    }

    @Test
    @DisplayName("Position이 같으면 같은 장소로 판단한다")
    void isSameLocation_withSamePosition_returnsTrue() {
        // given
        Position position = Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"));

        Place place1 = Place.createOf(
            "장소1",
            Address.of("주소1", "상세1", "11111"),
            position
        );

        Place place2 = Place.createOf(
            "장소2",
            Address.of("주소2", "상세2", "22222"),
            position
        );

        // when & then
        assertThat(place1.isSameLocation(place2)).isTrue();
    }

    @Test
    @DisplayName("Position이 다르면 다른 장소로 판단한다")
    void isSameLocation_withDifferentPosition_returnsFalse() {
        // given
        Place place1 = Place.createOf(
            "장소1",
            Address.of("주소1", "상세1", "11111"),
            Position.of(Double.valueOf("37.5665"), Double.valueOf("126.9780"))
        );

        Place place2 = Place.createOf(
            "장소2",
            Address.of("주소2", "상세2", "22222"),
            Position.of(Double.valueOf("37.5700"), Double.valueOf("126.9800"))
        );

        // when & then
        assertThat(place1.isSameLocation(place2)).isFalse();
    }
}
