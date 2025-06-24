package db.ninja.one_to_many;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id @GeneratedValue
    private Long id;

    private String title;

    // OneToMany 연관관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "image_order")
    private List<PostImage> images = new ArrayList<>();

    // Embeddable VO 타입을 사용한 OneToMany 연관관계
    @ElementCollection
    @CollectionTable(name = "post_image_vo", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image")
    private List<PostImageVO> imageVOs = new ArrayList<>();

    public Post(String title) {
        this.title = title;
    }

    /*
        @OneToMany(orphanRemoval = true) 연관 관계 주의사항

        하이버네이트는 컬렉션 엔티티의 참조를 이용하여 변경 사항을 추적한다
        단순히 컬렉션을 새로운 리스트로 교체하면 orphanRemoval을 삭제해야 되는데 기존 참조가 없어져서 추적이 불가능해지므로 예외가 발생한다
        A collection with orphan deletion was no longer referenced by the owning entity instance
        따라서 기존 리스트를 비우고 새로운 요소를 추가하는 방식으로 구현하는 것이 안전하다
     */
//    public void updateImagesThrowError(List<PostImage> newImages) {
//        this.images = newImages;
//    }

    public void updateImages(List<PostImage> newImages) {
        images.clear();
        images.addAll(newImages);

        for (PostImage image : newImages) {
            image.setPost(this);
        }
    }

    public void addImage(PostImage newImage) {
        images.add(newImage);
    }

    public void removeImage() {
        images.removeFirst();
    }

    // ElementCollection의 경우 @OneToMany 엔티티 컬렉션과 달리 값 타입이므로 컬렉션 필드에 대한 참조를 추적하지 않는다
    // 따라서 새로운 리스트로 교체해도 문제가 발생하지 않는다
    public void updateImageVOs(List<PostImageVO> newImageVOs) {
        imageVOs = newImageVOs;
    }

    public void addImageVO(PostImageVO newImageVO) {
        imageVOs.add(newImageVO);
    }

    public void removeImageVO(PostImageVO imageVO) {
        imageVOs.remove(imageVO);
    }

}
