package db.ninja.self_call;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Service
@RequiredArgsConstructor
public class OrderHelperService {

    private final OrderRepository orderRepository;

    @Transactional
    public void saveOrderWithTransaction() {
        System.out.println("현재 트랜잭션 활성화 여부: " + TransactionSynchronizationManager.isActualTransactionActive());
        orderRepository.save(new Order("test order"));
        throw new RuntimeException("주문 저장 실패");
    }

}
