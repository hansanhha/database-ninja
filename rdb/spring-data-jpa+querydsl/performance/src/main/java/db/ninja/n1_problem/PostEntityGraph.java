package db.ninja.n1_problem;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


// 네임드 엔티티 그래프를 정의한 엔티티 정의
@Entity
@Table(name = "n1_problem_post_entity_graph")
@Getter
@NamedEntityGraph(name = "post.writer", attributeNodes = { @NamedAttributeNode("user") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntityGraph {

    @Id @GeneratedValue
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public PostEntityGraph(String title, User user) {
        this.title = title;
        this.user = user;
    }

}
