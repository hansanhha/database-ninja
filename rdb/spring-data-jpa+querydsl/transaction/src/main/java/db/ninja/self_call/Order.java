package db.ninja.self_call;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "self_reference_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    private Long id;

    private String name;

    public Order(String name) {
        this.name = name;
    }

}
