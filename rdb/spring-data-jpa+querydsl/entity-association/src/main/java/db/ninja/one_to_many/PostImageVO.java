package db.ninja.one_to_many;


import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PostImageVO {

    private String url;

    public PostImageVO(String url) {
        this.url = url;
    }

}
