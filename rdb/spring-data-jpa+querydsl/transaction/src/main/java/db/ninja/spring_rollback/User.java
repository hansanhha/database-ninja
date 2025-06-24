package db.ninja.spring_rollback;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "spring_rollback_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue
    private Long id;

    private String name;

    public User(String name) {
        this.name = name;
    }

}
