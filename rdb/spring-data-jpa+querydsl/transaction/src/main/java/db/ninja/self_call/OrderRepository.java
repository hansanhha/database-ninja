package db.ninja.self_call;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository("selfReferenceOrderRepository")
public interface OrderRepository extends JpaRepository<Order, Long> {

}
