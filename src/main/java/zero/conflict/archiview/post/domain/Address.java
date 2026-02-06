package zero.conflict.archiview.post.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address {

    private String addressName; // 지번 주소
    private String roadAddressName; // 도로명 주소

    public static Address of(String addressName, String roadAddressName) {
        return new Address(addressName, roadAddressName);
    }
}