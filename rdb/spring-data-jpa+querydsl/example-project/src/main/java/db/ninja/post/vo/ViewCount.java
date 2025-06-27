package db.ninja.post.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor
public class ViewCount {

    @Column(name = "view_count", nullable = false)
    private int value;

    public void increase() {
        this.value++;
    }
}