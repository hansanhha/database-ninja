package db.ninja.one_to_many;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id @GeneratedValue
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String url;

    public PostImage(Post post, String url) {
        this.post = post;
        this.url = url;
    }

}
