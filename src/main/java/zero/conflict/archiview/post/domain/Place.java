package zero.conflict.archiview.post.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @jakarta.persistence.Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;

    private Address address;

    private Position position;

    private String nearestStationWalkTime;

    public static Place createOf(String name, Address address, Position position) {
        return createOf(name, address, position, null);
    }

    public static Place createOf(String name, Address address, Position position, String nearestStationWalkTime) {
        return Place.builder()
                .name(name)
                .address(address)
                .position(position)
                .nearestStationWalkTime(nearestStationWalkTime)
                .build();
    }

    public void update(String name, Address address) {
        validateName(name);
        validateAddress(address);

        this.name = name;
        this.address = address;
    }

    public boolean isSameLocation(Place other) {
        return this.position.equals(other.getPosition());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainException(PostErrorCode.INVALID_PLACE_NAME);
        }
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new DomainException(PostErrorCode.INVALID_PLACE_ADDRESS);
        }
    }

}
