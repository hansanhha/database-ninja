package db.ninja.review.repository;


import db.ninja.review.dto.ReviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


public interface ReviewCustomRepository {

    Slice<ReviewResponse> findAllByReviewerId(Long reviewerId, Pageable pageable);
}
