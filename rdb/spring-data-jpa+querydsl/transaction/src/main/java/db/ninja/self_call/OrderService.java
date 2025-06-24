package db.ninja.self_call;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderHelperService orderHelperService;

    /*
        자기 호출을 피하기 위해 OrderHelperService로 메서드를 분리하여 트랜잭션을 적용한다
     */
    public void placeOrder() {
        orderHelperService.saveOrderWithTransaction();
    }

}
