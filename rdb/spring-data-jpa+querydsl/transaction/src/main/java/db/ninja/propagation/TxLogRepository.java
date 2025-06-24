package db.ninja.propagation;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository("propagationTxLogRepository")
public interface TxLogRepository extends JpaRepository<TxLog, Long> {

}
