package db.ninja.review.entity;


import db.ninja.common.entity.BaseTimeEntity;
import db.ninja.review.vo.ReviewRating;
import lombok.*;
import db.ninja.post.entity.Post;
import db.ninja.user.User;
import jakarta.persistence.*;


@Entity
@Table(

        // 사용자 간 중복 리뷰 등록 방지
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_review_reviewer_reviewee_post", columnNames = {"reviewer_id", "reviewee_id", "post_id"})
        },

        // 리뷰어 통계 조회 시 성능 향상
        indexes = {
                @Index(name = "idx_review_reviewer", columnList = "reviewer_id"),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 탈퇴할 수 있으므로 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // 사용자가 탈퇴할 수 있으므로 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    // 게시글이 삭제될 수 있으므로 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Embedded
    @Column(nullable = false)
    private ReviewRating rating;

    @Column(length = 1000)
    private String comment;

    public static void validateReviewer(Review review, Long reviewerId) {
        if (!review.getReviewer().getId().equals(reviewerId))
            throw new IllegalArgumentException("리뷰어가 일치하지 않습니다.");
    }

}
