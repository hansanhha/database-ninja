package db.ninja.common.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class Location {

    @Column(length = 50, nullable = false)
    private String city;

    @Column(length = 50, nullable = false)
    private String district;

    @Column(length = 50, nullable = false)
    private String street;

    private String detail;

    public static Location setUserLocation(String city, String district, String street) {
        return  new Location(city, district, street, null);
    }

    public static Location setTransactionLocation(String city, String district, String street, String detail) {
        return new Location(city, district, street, detail);
    }

}
