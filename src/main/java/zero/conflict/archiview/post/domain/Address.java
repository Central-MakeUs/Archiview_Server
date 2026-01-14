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

    private String roadAddress;      // 도로명 주소
    private String detailAddress;    // 상세 주소
    private String zipCode;          // 우편번호

    public static Address of(String roadAddress, String detailAddress, String zipCode) {
        return new Address(roadAddress, detailAddress, zipCode);
    }
}