package db.ninja.duplication_insertion;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("경쟁 조건 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class DuplicationInsertionTests {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    String username = "test user";
    User user;
    CountDownLatch latch = new CountDownLatch(2);

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(username));
    }

    @AfterEach
    void tearDown() {
        postService.deleteAll();
        userRepository.deleteAll();
    }

    /*
        동시성으로 인해 게시글이 중복으로 작성될 수 있는 상황을 테스트한다
        Post 엔티티는 title 필드에 유니크 제약 조건이 걸려있다)

        PostService의 createPost 메서드는 title을 통해 이미 작성한 게시글이 있는지 확인하고 없을 경우에만 게시글을 작성한다
        이 때 동시에 두 개의 스레드가 createPost 메서드를 호출하면, 이 시점에는 작성한 게시글이 없기 때문에 두 개의 게시글이 작성될 수 있다

        이 테스트는 서비스 계층의 사전 검증 메서드를 통과하고 DB에 두 개의 게시글이 삽입하려는 시도로 인해 유니크 제약 조건을 위반하여 에러가 발생하는 것을 시뮬레이션한다
    */
    @Test
    void 유니크_제약조건으로_중복삽입을_방지할수있다() throws InterruptedException {
        Runnable createPost = () -> {
            try {
                // 두 트랜잭션 모두 postService.createPost 사전 검증을 통과한다
                postService.createPost("test post", username);
            } catch (DataIntegrityViolationException e) {
                System.out.println(Thread.currentThread().getName() + ": 유니크 제약 조건에 의해 게시글 중복 삽입 차단됨");
            } finally {
                latch.countDown();
            }
        };

        // 각각의 트랜잭션에서 게시글 동시 작성을 시도하지만 데이터베이스의 유니크 제약 조건에 의해 실패한다
        runTaskInTwoTransactions(createPost);
        latch.await();

        // 하나의 게시글만 작성된다
        assertThat(postService.getAll()).hasSize(1);
    }

    @Test
    void 비관적_락으로_중복삽입을_방지할수있다() throws InterruptedException {
        Runnable createPostWithLock = () -> {
            try {
                // 두 트랜잭션 중 하나는 락을 획득하지 못한다
                postService.createPostWithLock("test post", username);
            } catch (DataIntegrityViolationException e) {
                System.out.println(Thread.currentThread().getName() + ": 유니크 제약 조건에 의해 게시글 중복 삽입 차단됨");
            } finally {
                latch.countDown();
            }
        };

        // 각각의 트랜잭션에서 게시글 동시 작성을 시도하지만 비관적 락에 의해 실패한다
        runTaskInTwoTransactions(createPostWithLock);
        latch.await();

        // 하나의 게시글만 작성된다
        assertThat(postService.getAll()).hasSize(1);
    }

    private void runTaskInTwoTransactions(Runnable task) {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            executor.submit(task);
            executor.submit(task);
            executor.shutdown();
        }
    }
}
