package db.ninja.propagation;


import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Slf4j
@Service
@RequiredArgsConstructor
public class InnerService {

    private final TxLogRepository logRepository;

    @Transactional
    void innerRequired() {
        printCurrentTransactionStatus();

        logRepository.save(new TxLog("Inner REQUIRED"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void innerRequiresNew() {
        printCurrentTransactionStatus();

        logRepository.save(new TxLog("Inner REQUIRES_NEW"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    List<TxLog> getAllRequiresNew() {
        printCurrentTransactionStatus();

        return logRepository.findAll();
    }

    // JPA(하이버네이트)는 기본적으로 SAVEPOINT 기반의 중첩 트랜잭션을 지원하지 않는다
    @Transactional(propagation = Propagation.NESTED)
    void innerNested() {

    }

    @Transactional
    void innerRequiredThrows() {
        printCurrentTransactionStatus();

        logRepository.save(new TxLog("Inner EXCEPTION"));
        throw new RuntimeException("Inner Exception");
    }

    // 내부 REQUIRES_NEW 전파의 예외는 상위 메서드로 전파되어 상위 트랜잭션도 롤백시킬 수 있다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void innerRequiresNewThrows() {
        printCurrentTransactionStatus();

        logRepository.save(new TxLog("Inner EXCEPTION"));
        throw new RuntimeException("Inner Exception");
    }

    private void printCurrentTransactionStatus() {
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();

        System.out.println("현재 트랜잭션 이름: " + currentTransactionName);
        System.out.println("현재 트랜잭션 활성화 여부: " + transactionActive);
        System.out.println("현재 트랜잭션 동기화 활성화 여부: " + synchronizationActive);
        // 외부 트랜잭션의 중지 상태는 알 수 없다
        System.out.println("외부 트랜잭션 존재 여부: " + TxContextHolder.isOuterTransactionActive());
        System.out.println("내부 새 트랜잭션 생성 여부: " + TransactionAspectSupport.currentTransactionStatus().isNewTransaction());

    }

}
