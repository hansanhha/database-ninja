package db.ninja.post.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    @Column(name = "sales_amount", nullable = false)
    private int value;

    public Money(int value) {
        if (value < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        this.value = value;
    }

}
