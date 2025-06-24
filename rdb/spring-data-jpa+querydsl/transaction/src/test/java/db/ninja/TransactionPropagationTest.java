package db.ninja;


import db.ninja.propagation.OuterService;
import db.ninja.propagation.TxLog;
import db.ninja.propagation.TxLogRepository;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@DisplayName("트랜잭션 전파 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TransactionPropagationTest {

    @Autowired
    private OuterService outerService;

    @Autowired
    private TxLogRepository logRepository;

    @BeforeEach
    void clear() {
        logRepository.deleteAll();
    }

    @Test
    void 테스트클래스는_트랜잭션이_적용되지않는다() {
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean transactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        assertThat(transactionActive).isFalse();
        assertThat(transactionReadOnly).isFalse();
    }

    @Test
    void 내부와_외부_required_전파는_같은_트랜잭션을_공유한다() {
        outerService.requiredToRequired();
        assertThat(logRepository.findAll()).hasSize(2);
    }

    @Test
    void 내부_requiresNew_전파는_새로운_트랜잭션을_생성한다() {
        outerService.requiredToRequiresNew();
        assertThat(logRepository.findAll()).hasSize(2);
    }

    @Test
    void JPA_하이버네이트는_중첩트랜잭션을_지원하지않는다() {
        assertThatThrownBy(() -> outerService.requiredToNested()).isExactlyInstanceOf(NestedTransactionNotSupportedException.class);
    }

    @Test
    void 내부_requiresNew는_외부트랜잭션과_독립적인_영속성컨텍스트를_가진다() {
        List<TxLog> logs = outerService.saveLogRequiredAndGetAllLogInnerRequiresNew();

        assertThat(logs).isEmpty();
    }

    @Test
    void 내부_requiresNew에서_커밋하면_외부에서_데이터베이스로부터_접근할수있다() {
        List<TxLog> logs = outerService.getLogsInSaveInnerRequiresNew();

        assertThat(logs).hasSize(1);
        assertThat(logs).extracting(TxLog::getMessage).containsOnly("Inner REQUIRES_NEW");
    }

    @Test
    void 외부_롤백은_내부_requiresNew에_영향을주지못한다() {
        assertThatThrownBy(() -> outerService.requiredToRequiresNewAndOuterThrows()).isInstanceOf(RuntimeException.class);

        List<TxLog> logs = logRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs).extracting(TxLog::getMessage).containsOnly("Inner REQUIRES_NEW");
    }

    @Test
    void 외부_롤백으로_내부_required도_롤백한다() {
        assertThatThrownBy(() -> outerService.requiredToRequiredAndOuterThrows()).isInstanceOf(RuntimeException.class);

        List<TxLog> logs = logRepository.findAll();
        assertThat(logs).hasSize(0);
    }

    @Test
    void 내부_required예외로_인해_외부가_롤백된다() {
        assertThatThrownBy(() -> outerService.requiredToRequiredAndInnerThrows()).isInstanceOf(RuntimeException.class);

        List<TxLog> logs = logRepository.findAll();
        assertThat(logs).isEmpty();
    }

    @Test
    void 내부_required예외를_캐치해도_외부는_롤백된다() {
        assertThatThrownBy(() -> outerService.requiredToRequiredAndInnerThrowsAndCatch()).isInstanceOf(RuntimeException.class);

        List<TxLog> logs = logRepository.findAll();
        assertThat(logs).isEmpty();
    }

    @Test
    void 내부_requiresNew예외로_외부가_롤백된다() {
        assertThatThrownBy(() -> outerService.requiredToRequiresNewAndInnerThrows()).isInstanceOf(RuntimeException.class);

        List<TxLog> logs = logRepository.findAll();
        assertThat(logs).isEmpty();
    }

    @Test
    void 내부_requiresNew예외를_캐치하면_외부는_롤백하지않는다() {
        outerService.requiredToRequiresNewAndInnerThrowsAndCatch();

        List<TxLog> logs = logRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs).extracting(TxLog::getMessage).containsOnly("Outer REQUIRED");
    }

}
