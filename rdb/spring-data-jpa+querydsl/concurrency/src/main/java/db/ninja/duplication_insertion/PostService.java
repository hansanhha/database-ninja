package db.ninja.duplication_insertion;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void createPost(String title, String username) {

        // 동시에 두 개 이상의 스레드(트랜잭션)에서 접근하면 둘 다 통과되어 중복 삽입이 발생할 수 있다
        if (postRepository.findByUsername(username).isPresent()) {
            System.out.println(Thread.currentThread().getName() +": 이미 작성한 글이 있습니다");
            return;
        }

        System.out.println(Thread.currentThread().getName() + ": 사전 검증 통과");
        System.out.println(Thread.currentThread().getName() + ": 게시글 작성 중");
        User user = userRepository.findByUsername(username).orElseThrow();
        postRepository.save(new Post(title, user));
        System.out.println(Thread.currentThread().getName() + ": 게시글 작성 완료");
    }

    public void createPostWithLock(String title, String username) {
        System.out.println(Thread.currentThread().getName() + ": 유저 락 획득 시도 중");

        // User 엔티티를 기반으로 비관적 락 (PESSIMISTIC_WRITE)을 사용하여 동시성 문제를 방지한다
        User user = userRepository.findByUsernameWithLock(username).orElseThrow();

        System.out.println(Thread.currentThread().getName() + ": 유저 락 획득 성공");
        System.out.println(Thread.currentThread().getName() + ": 게시글 작성 중");

        if (postRepository.findByUsername(username).isPresent()) {
            System.out.println(Thread.currentThread().getName() + ": 이미 작성한 글이 있습니다");
            return;
        }

        postRepository.save(new Post(title, user));
        System.out.println(Thread.currentThread().getName() + ": 게시글 작성 완료");
    }

    public Iterable<Post> getAll() {
        return postRepository.findAll();
    }

    public void deleteAll() {
        postRepository.deleteAll();
    }

}
