package db.ninja.duplication_insertion;


import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /*
        게시글 중복 삽입 방지를 위해 User 엔티티에 비관적 락을 건다
        도메인 모델이 왜곡되고 락 범위가 커서 부작용이 발생할 수 있는 방식이므로 고정된 락 대상을 따로 만들어 사용하는 것이 비교적 좋다
    */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithLock(String username);

}
