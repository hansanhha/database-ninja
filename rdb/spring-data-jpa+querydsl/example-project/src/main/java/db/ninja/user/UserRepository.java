package db.ninja.user;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByNickname(String nickname);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

}
