package db.ninja;


import db.ninja.spring_rollback.UserRepository;
import db.ninja.spring_rollback.UserService;
import jakarta.persistence.SqlResultSetMapping;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DisplayName("스프링의 롤백 전략 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class SpringRollbackStrategyTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void 체크예외는_자동_롤백되지_않는다() {
        try {
            userService.createUserWithCheckedException();
        } catch (Exception ignored) {}

        // 체크 예외로 인해 롤백되지 않는다
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void 언체크예외는_자동_롤백된다() {
        try {
            userService.createUserWithUncheckedException();
        } catch (Exception ignored) {}

        // 언체크 예외로 인해 롤백된다
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void rollbackFor_속성을_사용하면_체크예외도_롤백된다() {
        try {
            userService.createUserWithCheckedExceptionWithRollback();
        } catch (Exception ignored) {}

        // 롤백 대상 예외로 지정했기 때문에 롤백된다
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void noRollbackFor_속성을_사용하면_언체크예외도_롤백되지_않는다() {
        try {
            userService.createUserWithUncheckedExceptionWithNoRollback();
        } catch (Exception ignored) {}

        // 롤백 대상 예외로 지정하지 않았기 때문에 롤백되지 않는다
        assertThat(userRepository.findAll()).hasSize(1);
    }


}
