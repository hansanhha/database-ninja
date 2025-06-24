package db.ninja.review.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import db.ninja.review.entity.QReview;
import db.ninja.review.dto.ReviewResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements  ReviewCustomRepository {

    private final JPAQueryFactory query;
    private final QReview review = QReview.review;

    @Override
    public Slice<ReviewResponse> findAllByReviewerId(Long reviewerId, Pageable pageable) {
        List<ReviewResponse> result = query
                .select(
                        Projections.constructor(ReviewResponse.class,
                                review.id,
                                review.reviewer.id,
                                review.reviewee.id,
                                review.post.id,
                                review.post.title,
                                review.post.productImages.get(0).url,
                                review.comment,
                                review.rating.value,
                                review.createdAt
                        )
                )
                .from(review)
                .leftJoin(review.reviewer).on(review.id.eq(review.reviewer.id))
                .leftJoin(review.reviewee).on(review.id.eq(review.reviewee.id))
                .leftJoin(review.post).on(review.id.eq(review.post.id))
                .where(review.reviewer.id.eq(reviewerId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // hasNext 여부를 판단하기 위해 +1
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.remove(pageable.getPageSize());
        return new SliceImpl<>(result, pageable, hasNext);
    }

}
