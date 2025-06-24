package db.ninja.review.repository;


import db.ninja.review.entity.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface ReviewRepository extends CrudRepository<Review, Long>, ReviewCustomRepository{

    @Query("SELECT r " +
            "FROM Review r " +
            "JOIN FETCH r.reviewer " +
            "WHERE r.id = :reviewId")
    Optional<Review> findByIdFetchJoinReviewer(Long reviewId);

}
