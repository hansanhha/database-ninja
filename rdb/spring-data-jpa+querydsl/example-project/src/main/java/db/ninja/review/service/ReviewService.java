package db.ninja.review.service;


import db.ninja.post.entity.Post;
import db.ninja.post.service.PostQueryService;
import db.ninja.review.entity.Review;
import db.ninja.review.vo.ReviewRating;
import db.ninja.review.dto.ReviewResponse;
import db.ninja.review.repository.ReviewRepository;
import db.ninja.user.User;
import db.ninja.user.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostQueryService postQueryService;
    private final UserQueryService userQueryService;

    public Long createReview(Long userId, Long targetUserId, Long tradePostId, String comment, int rating) {
        User reviewer = userQueryService.getUserById(userId);
        User reviewee = userQueryService.getUserById(targetUserId);
        Post post = postQueryService.getPostById(tradePostId);

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .comment(comment)
                .rating(new ReviewRating(rating))
                .post(post)
                .build();

        Review save = reviewRepository.save(review);
        return save.getId();
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findByIdFetchJoinReviewer(reviewId).orElseThrow();
        Review.validateReviewer(review, userId);
        reviewRepository.delete(review);
    }

    public Slice<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        return reviewRepository.findAllByReviewerId(userId, pageable);
    }

}
