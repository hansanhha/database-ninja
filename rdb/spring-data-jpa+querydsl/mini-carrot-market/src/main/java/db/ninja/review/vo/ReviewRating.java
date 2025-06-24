package db.ninja.review.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRating {

    private static final int MIN = 1;
    private static final int MAX = 5;

    @Column(name = "rating", nullable = false)
    private int value;

    public ReviewRating(int value) {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(String.format("별점은 %d부터 %d점까지만 지정할 수 있습니다", MIN, MAX));
        }

        this.value = value;
    }
}
