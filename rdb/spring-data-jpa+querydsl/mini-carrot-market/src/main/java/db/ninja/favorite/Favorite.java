package db.ninja.favorite;


import db.ninja.common.entity.BaseTimeEntity;
import db.ninja.post.entity.Post;
import db.ninja.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(
        name = "favorites",

        // 사용자와 게시글 간 중복 찜 등록 방지
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_favorite_user_post", columnNames = {"user_id", "post_id"})
        },

        // 관심 게시글 목록, 게시글별 관심 통계 조회 시 성능 향상
        indexes = {
                @Index(name = "idx_favorite_user", columnList = "user_id"),
                @Index(name = "idx_favorite_post", columnList = "post_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public Favorite(User user, Post post) {
        this.user = user;
        this.post = post;
    }

}