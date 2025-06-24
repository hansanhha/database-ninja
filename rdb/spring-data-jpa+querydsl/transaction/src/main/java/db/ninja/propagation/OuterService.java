package db.ninja.propagation;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Service
@RequiredArgsConstructor
public class OuterService {

    private final InnerService innerService;
    private final TxLogRepository logRepository;

    @Transactional
    public void requiredToRequired() {
        printCurrentTransactionStatus();
        TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

        logRepository.save(new TxLog("Outer REQUIRED"));
        innerService.innerRequired();
    }

    @Transactional
    public void requiredToRequiresNew() {
        printCurrentTransactionStatus();
        TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

        logRepository.save(new TxLog("Outer REQUIRED"));
        innerService.innerRequiresNew();
    }

    @Transactional
    public void requiredToNested() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerNested();
        } finally {
            TxContextHolder.clear();
        }

    }

    /*
        현재 트랜잭션에서 엔티티를 영속해도 내부 REQUIRES_NEW 전파는 별도의 영속성 컨텍스트를 가진다
        따라서 현재 트랜잭션에서 영속한 엔티티에 접근할 수 없다
     */
    @Transactional
    public List<TxLog> saveLogRequiredAndGetAllLogInnerRequiresNew() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            TxLog saved = logRepository.save(new TxLog("Outer REQUIRED"));
            Long id = saved.getId();
            return innerService.getAllRequiresNew();
        } finally {
            TxContextHolder.clear();
        }

    }

    // 내부 REQUIRES_NEW 트랜잭션이 커밋하면 flush된 시점이기 때문에 외부 트랜잭션에서 접근할 수 있다 (데이터베이스)
    @Transactional(readOnly = true)
    public List<TxLog> getLogsInSaveInnerRequiresNew() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            innerService.innerRequiresNew();
            return logRepository.findAll();
        } finally {
            TxContextHolder.clear();
        }
    }

    // 외부에서 예외가 발생하더라도 내부 REQUIRES_NEW 전파는 별도의 트랜잭션으로 처리되므로 롤백되지 않는다
    // 현재 트랜잭션만 롤백되고, 내부 REQUIRES_NEW 트랜잭션은 커밋된다
    @Transactional
    public void requiredToRequiresNewAndOuterThrows() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerRequiresNew();

            throw new RuntimeException("Outer Exception");
        } finally {
            TxContextHolder.clear();
        }
    }

    // 외부의 예외가 하위 메서드에 전파되지 않지만 동일한 트랜잭션을 사용하기 때문에 하위 트랜잭션도 결국 롤백된다
    @Transactional
    public void requiredToRequiredAndOuterThrows() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerRequired();

            throw new RuntimeException("Outer Exception");
        } finally {
            TxContextHolder.clear();
        }
    }

    @Transactional
    public void requiredToRequiredAndInnerThrows() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerRequiredThrows();
        } finally {
            TxContextHolder.clear();
        }

    }

    // 내부 REQUIRED 전파는 외부와 동일한 트랜잭션을 사용한다
    // 따라서 내부에서 예외가 발생하면 이미 롤백이 되었기 때문에 외부에서 예외를 처리해도 외부 트랜잭션도 롤백된다
    @Transactional
    public void requiredToRequiredAndInnerThrowsAndCatch() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerRequiredThrows();
        } catch (RuntimeException ignored) {

        } finally {
            TxContextHolder.clear();
        }

    }

    // 내부 REQUIRES_NEW 전파의 예외가 상위 메서드로 전달될 때 처리하지 않으면 상위 트랜잭션도 롤백된다
    @Transactional
    public void requiredToRequiresNewAndInnerThrows() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerRequiresNewThrows();
        } finally {
            TxContextHolder.clear();
        }

    }

    // 내부 REQUIRES_NEW 전파의 예외가 상위 메서드로 전달될 때 처리하면 상위 트랜잭션은 롤백되지 않는다
    @Transactional
    public void requiredToRequiresNewAndInnerThrowsAndCatch() {
        try {
            printCurrentTransactionStatus();
            TxContextHolder.setOuterTxContext(TransactionSynchronizationManager.isActualTransactionActive());

            logRepository.save(new TxLog("Outer REQUIRED"));
            innerService.innerRequiresNewThrows();
        } catch (Exception ignored) {

        } finally {
            TxContextHolder.clear();
        }

    }

    private void printCurrentTransactionStatus() {
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();


        System.out.println("현재 트랜잭션 이름: " + currentTransactionName);
        System.out.println("현재 트랜잭션 활성화 여부: " + transactionActive);
        System.out.println("현재 트랜잭션 동기화 활성화 여부: " + synchronizationActive);
    }


}
