package db.ninja.duplication_insertion;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity(name = "concurrency_post")
// 유니크 제약을 통한 중복 삽입 방지
@Table(name = "concurrency_post",
        uniqueConstraints = @UniqueConstraint(name = "uk_race_condition_post_title", columnNames = {"title"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Post(String title, User user) {
        this.title = title;
        this.user = user;
    }

}
