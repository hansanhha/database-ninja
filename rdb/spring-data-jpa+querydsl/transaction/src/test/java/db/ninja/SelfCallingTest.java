package db.ninja;


import db.ninja.self_call.Order;
import db.ninja.self_call.OrderRepository;
import db.ninja.self_call.OrderService;
import db.ninja.self_call.SelfCallingOrderService;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DisplayName("자기 호출로 인한 @Transactional 미적용 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class SelfCallingTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SelfCallingOrderService selfCallingOrderService;

    @Autowired
    private OrderService orderService;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 자기호출은_트랜잭션이_적용되지않는다() {
        try {
            selfCallingOrderService.placeOrder();
        } catch (Exception ignored) {}

        List<db.ninja.self_call.Order> orders = orderRepository.findAll();
        assertThat(orders).isNotEmpty();
    }

    @Test
    void 외부호출은_트랜잭션이_적용된다() {
        try {
            orderService.placeOrder();
        } catch (Exception ignored) {}

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).isEmpty();
    }

}
