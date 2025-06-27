package db.ninja.post.entity;


import db.ninja.category.Category;
import db.ninja.common.entity.BaseTimeEntity;
import db.ninja.common.vo.Location;
import db.ninja.post.vo.Money;
import db.ninja.post.vo.PostStatus;
import db.ninja.post.vo.ViewCount;
import db.ninja.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;


@Entity
@Table(name = "posts")
@NamedEntityGraph(name = "author", attributeNodes = @NamedAttributeNode("author"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    private static final int PRODUCT_IMAGE_MAXIMUM = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Lob
    @Column(nullable = false)
    private String content;

    @Embedded
    private Money salesAmount;

    @ElementCollection
    @CollectionTable(name = "post_product_images", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image", nullable = false)
    private List<ProductImage> productImages;

    @Embedded
    @Column(nullable = false)
    private Location tradeLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus beforeHiddenStatus;

    @Embedded
    @Column(nullable = false)
    private ViewCount viewCount = new ViewCount();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_partner_id")
    private User tradePartner;

    private LocalDateTime tradeDate;

    @Builder
    public Post(User user, String title, Category category, String content, Money salesAmount, List<ProductImage> productImages, Location tradeLocation) {
        if (productImages.isEmpty()) {
            throw new IllegalArgumentException("최소 1장의 상품 이미지를 업로드해주세요");
        }

        if (productImages.size() > PRODUCT_IMAGE_MAXIMUM) {
            throw new IllegalArgumentException("상품 이미지는 최대 10장까지만 업로드할 수 있습니다");
        }

        validateTradeLocationDetail(tradeLocation);

        this.author = user;
        this.title = title;
        this.category = category;
        this.content = content;
        this.salesAmount = salesAmount;
        this.tradeLocation = tradeLocation;
        this.status = PostStatus.ON_SALE;
        this.viewCount = new ViewCount();
        this.tradePartner = null;
        this.tradeDate = null;
    }

    // 기능 메서드
    public void increaseViewCount() {
        this.viewCount.increase();
    }

    public void reserve(User tradePartner, LocalDateTime tradeDate) {
        if (this.status.canTransition(PostStatus.RESERVED)) {
            throw new IllegalStateException("예약할 수 없는 상태입니다");
        }

        this.tradePartner = tradePartner;
        this.tradeDate = tradeDate;
        this.status = PostStatus.RESERVED;
    }

    public void cancelReservation() {
        this.tradePartner = null;
        this.tradeDate = null;
        this.status = PostStatus.ON_SALE;
    }

    public void hide() {
        if (this.status.canTransition(PostStatus.HIDDEN)) {
            throw new IllegalStateException("예약된 게시글은 숨길 수 없습니다");
        }

        this.beforeHiddenStatus = this.status;
        this.status = PostStatus.HIDDEN;
    }

    public void unhide() {
        this.status = beforeHiddenStatus;
    }

    public void sold() {
        if (this.status.canTransition(PostStatus.SOLD)) {
            throw new IllegalStateException("예약된 게시글만 판매 완료 상태로 전환할 수 있습니다");
        }

        this.status = PostStatus.SOLD;
    }

    public void updateTradeDate(LocalDateTime tradeDate) {
        if (!status.equals(PostStatus.RESERVED)) {
            throw new IllegalStateException("예약된 게시글만 거래 일시를 변경할 수 있습니다");
        }

        this.tradeDate = tradeDate;
    }

    public void update(String title, String content, Category category, List<ProductImage> productImages, Money price, Location tradeLocation) {
        validateTradeLocationDetail(tradeLocation);

        this.title = title;
        this.content = content;
        this.category = category;
        this.productImages = productImages;
        this.salesAmount = price;
        this.tradeLocation = tradeLocation;
    }

    public void validateAuthor(User user) {
        if (!this.author.equals(user)) {
            throw new IllegalArgumentException("게시글 작성자만 수정할 수 있습니다");
        }
    }

    private void validateTradeLocationDetail(Location tradeLocation) {
        if (!StringUtils.hasText(tradeLocation.getDetail())) {
            throw new IllegalArgumentException("거래 장소의 상세 위치를 입력해주세요");
        }
    }

}