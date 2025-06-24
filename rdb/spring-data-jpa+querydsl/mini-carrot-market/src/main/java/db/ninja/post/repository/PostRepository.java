package db.ninja.post.repository;


import db.ninja.post.entity.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface PostRepository extends CrudRepository<Post, Long>, PostCustomRepository {

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN FETCH p.author a " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long postId);

}
