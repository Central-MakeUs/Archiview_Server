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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class Place extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Address Address;

    private Position position;

    public static Place createOf(String name, Address address, Position position) {
        return Place.builder()
                .name(name)
                .Address(address)
                .position(position)
                .build();
    }

    public void update(String name, Address address) {
        validateName(name);
        validateAddress(address);

        this.name = name;
        this.Address = address;
    }

    public boolean isSameLocation(Place other) {
        return this.position.equals(other.getPosition());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("장소명은 필수입니다.");
        }
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("주소는 필수입니다.");
        }
    }

}
