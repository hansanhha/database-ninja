package db.ninja.one_to_many;


import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;


public interface PostRepository extends CrudRepository<Post, Long> {

    @EntityGraph(attributePaths = "images")
    Optional<Post> findById(Long id);
}
