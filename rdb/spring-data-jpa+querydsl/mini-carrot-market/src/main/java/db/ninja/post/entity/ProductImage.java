package db.ninja.post.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000,  nullable = false)
    private String url;

    public ProductImage(String name, String url) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("이미지 이름은 필수입니다.");
        if (url == null || url.isBlank()) throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        this.name = name;
        this.url = url;
    }

}
