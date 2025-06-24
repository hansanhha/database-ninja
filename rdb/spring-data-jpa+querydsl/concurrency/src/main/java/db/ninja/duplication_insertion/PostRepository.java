package db.ninja.duplication_insertion;


import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository("concurrencyPostRepository")
public interface PostRepository extends CrudRepository<Post, Long> {

    @Query("SELECT p FROM concurrency_post p JOIN p.user u WHERE u.username = :username")
    Optional<Post> findByUsername(@Param("username") String username);

}
