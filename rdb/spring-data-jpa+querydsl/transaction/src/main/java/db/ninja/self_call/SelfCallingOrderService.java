package db.ninja.self_call;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Service
@RequiredArgsConstructor
public class SelfCallingOrderService {

    private final OrderRepository orderRepository;

    /*
        @Transactional 메서드를 자기 호출하는 경우
        트랜잭션 프록시를 거칠 수 없기 때문에 실질적으로 트랜잭션이 적용되지 않는다
        따라서 saveOrder 메서드에서 예외가 발생해도 트랜잭션이 롤백되지 않는다

        이러한 문제를 해결하려면 내부 호출을 외부 메서드로 분리해서 트랜잭션 프록시가 적용되도록 해야 한다
     */
    public void placeOrder() {
        saveOrder();
    }

    @Transactional
    public void saveOrder() {
        System.out.println("현재 트랜잭션 활성화 여부: " + TransactionSynchronizationManager.isActualTransactionActive());
        orderRepository.save(new Order("test order"));
        throw new RuntimeException("주문 저장 실패");
    }

}
