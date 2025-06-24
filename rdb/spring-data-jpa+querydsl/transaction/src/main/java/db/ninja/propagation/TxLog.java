package db.ninja.propagation;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "propagation_tx_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TxLog {

    @Id @GeneratedValue
    private Long id;

    private String message;

    public TxLog(String message) {
        this.message = message;
    }

}
