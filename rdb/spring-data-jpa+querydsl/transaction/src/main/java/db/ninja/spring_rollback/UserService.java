package db.ninja.spring_rollback;


import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 스프링은 체크 예외를 기본적으로 롤백하지 않는다
    @Transactional
    public void createUserWithCheckedException() throws IOException {
        userRepository.save(new User("test user"));

        throw new IOException();
    }

    // 롤백되는 체크 예외
    @Transactional(rollbackFor = IOException.class)
    public void createUserWithCheckedExceptionWithRollback() throws IOException {
        userRepository.save(new User("test user"));

        throw new IOException();
    }

    // 스프링은 언체크 예외를 기본적으로 롤백한다
    @Transactional
    public void createUserWithUncheckedException() {
        userRepository.save(new User("test user"));

        throw new RuntimeException();
    }

    // 롤백되지 않는 언체크 예외
    @Transactional(noRollbackFor = RuntimeException.class)
    public void createUserWithUncheckedExceptionWithNoRollback() {
        userRepository.save(new User("test user"));

        throw new RuntimeException();
    }

}
