package db.ninja.favorite;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface FavoriteRepository extends CrudRepository<Favorite, Long> {

    @Query("SELECT exists(f) " +
            "FROM Favorite f " +
            "WHERE f.user.id = :userId AND f.post.id = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

}
