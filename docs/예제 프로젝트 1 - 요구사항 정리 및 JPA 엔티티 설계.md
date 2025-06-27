[이전으로](../README.md)

[JPA 엔티티 설계 흐름](#jpa-엔티티-설계-흐름)

[공통 엔티티, VO](#공통-엔티티-vo)

[User](#user)

[Post](#post)

[Favorite](#favorite)

[Category](#category)

[Review](#review)

[Chat](#chat)


## JPA 엔티티 설계 흐름

[프로젝트 전체 기능](../README.md#예제-프로젝트-소개)

일반적으로 기능은 기능 명세 -> 도메인 요구사항 -> 엔티티 설계 -> 기능 구현의 단계를 거친다

여기서는 기능 구현을 제외한 앞의 세 가지 단계를 거쳐서 최종적으로 엔티티를 코드로 구현한다

| 항목      | 설명                                                                              |
|---------|---------------------------------------------------------------------------------|
| 기능 명세   | 사용자의 행동을 기준으로 작성된 요구사항 목록을 말한다. 사용자가 무엇을 할 수 있는지를 정의한다                          |
| 도메인 요구사항 | 기능 명세를 내부 도메인 모델의 관점에서 해석한 것이다. 도메인이 책임지는 역할, 관련 모델, 상태, 규칙 등을 서술한다 (비즈니스 규칙 중심) |
| 엔티티 설계  | 도메인 요구사항을 구체적인 JPA 엔티티로 모델링하는 것을 말한다. 필드, 연관 관계, 메서드, 제약 조건 등을 정의한다              |

예제 프로젝트 ([mini_carrot_market](../mini-carrot-market))는 당근을 벤치마킹한 간단한 서비스로 사용자가 판매할 중고 상품 게시글을 올리면 채팅을 통해 거래를 진행하는 기능을 제공한다

아래의 각 도메인마다 **기능 명세 및 요구사항을 고려하여** JPA 엔티티를 설계하도록 한다

| 도메인        | JPA 엔티티                               |
|------------|---------------------------------------|
| 사용자        | User                                  |
| 중고 상품 게시글  | Post, PostImage                       |
| 중고 상품 카테고리 | Category                              |
| 관심 상품 (좋아요) | Favorite                              |
| 채팅         | ChatRoom, ChatMessage, ChatParticipant |
| 리뷰         | Review                                |

### JPA 엔티티와 관련된 개념들

아래의 링크는 엔티티 설계할 때 궁금하거나 알아두면 좋을 정보들이다

[엔티티 ID 전략](./JPA%20엔티티%20-%20ID%20전략.md)

[도메인 모델과 JPA 엔티티 (DDD와의 관계)](./JPA%20엔티티%20-%20도메인%20모델%20(DDD와의%20관계).md)

[JPA 엔티티의 시간 자료형 선택지](./JPA%20엔티티%20-%20시간%20자료형%20선택지.md)

[단방향 연관 관계와 양방향 연관 관계](./JPA%20엔티티%20-%20연관관계.md)

[리치 도메인 모델과 애너미 도메인 모델, 도메인 서비스](./기타%20-%20리치%20도메인%20모델과%20애너믹%20도메인%20모델,%20도메인%20서비스.md)


## 공통 엔티티, VO

본격적으로 JPA 엔티티를 설계하기에 앞서 프로젝트에서 공통적으로 사용될 엔티티와 값 객체에 대해 알아보자

BaseTimeEntity: 생성 시간과 수정 시간을 추적하는 데 사용된다

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false, nullable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime lastModifiedAt;

}
```

Location: 사용자의 동네 위치 설정, 거래 장소 위치 설정에 사용된다

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class Location {

    @Column(length = 50, nullable = false)
    private String city;

    @Column(length = 50, nullable = false)
    private String district;

    @Column(length = 50, nullable = false)
    private String street;

    private String detail;

    public static Location setUserLocation(String city, String district, String street) {
        return  new Location(city, district, street, null);
    }

    public static Location setTransactionLocation(String city, String district, String street, String detail) {
        return new Location(city, district, street, detail);
    }

}
```


## User

User 도메인은 사용자가 시스템을 이용하기 위해 필요한 정보를 나타낸다

### 1. 기능 명세
- 사용자는 회원가입을 할 수 있다
- 사용자는 이메일과 비밀번호로 로그인할 수 있다
- 사용자는 본인의 프로필을 조회할 수 있다
- 사용자는 회원 탈퇴를 할 수 있다
- 사용자는 본인의 위치 정보를 설정할 수 있다

### 2. 도메인 요구사항

#### 2-1. 주요 책임
- 사용자 정보 등록 및 인증
- 프로필 정보 제공
- 상태 관리 (정상, 탈퇴 등)
- 위치 정보 설정

#### 2-2. 도메인 모델 및 연관 관계 설정

User: 사용자 정보를 포함하고 상태를 관리하는 모델

User는 여러 개의 Post, Favorite, ChatMessage, Trade 등과 관계를 맺는다 (1:N)

다만 연관 관계는 다대일 방향으로만 설계하여 연관 객체의 생명 주기를 User에 종속시키지 않는다

Location은 엔티티가 아닌 VO로 관리한다

#### 2-3. 기능 요구사항

##### 사용자 등록 및 인증 정보
- 이메일(로그인 ID), 비밀번호, 닉네임, 최소 1개의 위치 정보를 입력하여 가입할 수 있다
- 이메일은 시스템에서 고유해야 하고 최소 5자 이상, 최대 100자로 제한된다
- 비밀번호는 암호화되어 저장되어야 하며 길이는 최소 20자 이상, 최대 100자 이하로 제한된다
- 기본 정보(닉네임, 프로필 이미지 등)는 프로필 조회에 사용된다

##### 닉네임
- 닉네임은 시스템에서 고유해야 하며 길이 제한(2자 ~ 20자)를 가진다
- 닉네임 변경 시 일주일동안 변경할 수 없다
- 마지막 닉네임 변경 시각을 기준으로 검증 로직을 구현한다

##### 비밀번호 변경
- 비밀번호 변경 시 기존 비밀번호 검증이 필요하다

##### 위치 정보 관리
- 사용자는 자신의 위치 정보(Location)를 설정할 수 있다
- 위치 정보는 도로명 주소를 기반으로 하는 VO 객체로 관리된다
- 사용자는 최대 3개의 위치 정보를 관리할 수 있다

##### 계정 관리
- 사용자 탈퇴 요청 시 Soft Delete 방식 또는 상태 플러그로 처리하고 1주일의 기간이 지나면 계정을 삭제한다
- 연관 데이터(Post, TradeReview 등)는 계정 삭제 후에도 보존할 수 있다

### 3. User 엔티티 및 VO 구현

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    // 제약조건 상수
    private static final int LOCATION_HOLD_MAXIMUM= 3;
    private static final long NICKNAME_CHANGE_INTERVAL = Duration.ofDays(7).toDays();
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // 필드
    // 엔티티 기본키 지정
    @Id
    // 기본키 생성 전략 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 컬럼 속성 지정
    @Column(nullable = false, length = 100, unique = true)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    private LocalDateTime lastNicknameChangedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // 값 타입 컬렉션 매핑 시 사용
    @ElementCollection
    // 별도 테이블에 위치 정보 저장
    @CollectionTable(name = "user_locations", joinColumns = @JoinColumn(name = "user_id"))
    private List<Location> locations = new ArrayList<>();

    // 생성자
    @Builder
    public User(String username, String password, String nickname, Location location) {
        if (isValidEmail(username)) {
            throw new IllegalArgumentException("이메일 형식의 아이디만 사용할 수 있습니다");
        }

        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.locations.add(location);
        this.status = UserStatus.ACTIVE;
        this.lastNicknameChangedAt = null;
    }

    // 도메인 규칙 메서드 (비즈니스 규칙 메서드)
    public static boolean isValidEmail(String email) {
        return EMAIL_REGEX.matcher(email).matches();
    }

    public boolean canChangeNickname(LocalDateTime now) {
        return lastNicknameChangedAt == null || now.isAfter(lastNicknameChangedAt.plusDays(NICKNAME_CHANGE_INTERVAL));
    }

    // 도메인 행위 메서드 (상태 변경 메서드)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String newNickname) {
        LocalDateTime now = LocalDateTime.now();

        if (!canChangeNickname(now)) {
            throw new IllegalArgumentException(
                    lastNicknameChangedAt.plusDays(NICKNAME_CHANGE_INTERVAL).toString().concat("이후 닉네임을 변경할 수 있습니다"));
        }

        this.nickname = newNickname;
        this.lastNicknameChangedAt = now;
    }

    public void addLocation(Location location) {
        if (locations.size() > LOCATION_HOLD_MAXIMUM) {
            throw new IllegalArgumentException("최대 3개의 위치만 등록할 수 있습니다");
        }

        locations.add(location);
    }

    public void removeLocation(Location location) {
        if (!locations.contains(location)) {
            throw new IllegalArgumentException("등록되지 않은 위치입니다");
        }

        if (locations.size() == 1) {
            throw new IllegalArgumentException("최소 하나의 위치는 등록되어 있어야 합니다");
        }

        locations.remove(location);
    }

    public void delete() {
        this.status = UserStatus.DELETE_SCHEDULED;
    }

}
```

```java
public enum UserStatus {

    ACTIVE,
    DELETE_SCHEDULED

}
```


## Post

Post 도메인은 사용자가 중고 물품 거래를 위한 게시글 작성과 검색 및 조회를 의미한다

판매자는 물품의 정보(제목, 내용, 이미지, 가격 등)와 거래 희망 위치를 설정하며, 다른 사용자는 해당 게시글을 열람하고 구매를 요청할 수 있다

### 1. 기능 명세
- 사용자는 게시글을 작성할 수 있다
- 사용자는 게시글을 수정하거나 삭제할 수 있다
- 사용자는 게시글의 거래 상태를 변경할 수 있다 (판매 중, 예약 중, 거래 완료)
- 판매자는 예약 중인 게시글의 거래 일시를 변경할 수 있다
- 판매자는 게시글을 숨기거나 다시 노출시킬 수 있다
- 판매자는 예약된 게시글은 숨길 수 없으며, 다시 노출시키는 경우 이전 상태를 유지한다
- 사용자는 게시글을 위치를 기반으로 조회할 수 있다
- 게시글 검색/정렬: 사용자는 게시글을 검색하고 최신순/거래순/가격순으로 정렬할 수 있다
- 게시글은 카테고리, 위치, 이미지, 가격, 내용 정보를 포함한다
- 게시글은 조회 수와 관심 수를 가진다

### 2. 도메인 요구사항

#### 2-1. 주요 책임
- 상품 게시글의 생성, 수정, 삭제
- 게시글 상태 변경 및 게시글 숨김 처리
- 이미지, 카테고리, 가격 등 핵심 정보 관리
- 조회수, 관심수와 같은 통계 정보 관리


#### 2-2. 도메인 모델 및 연관 관계 설정

Post: 게시글 정보를 포함하고 상태를 관리하는 모델

Post는 하나의 User에 소속된다 (ManyToOne)

Post는 하나의 Category를 가진다 (ManyToOne)

Post는 하나의 Location을 가진다 (Embedded VO)

하나의 Post는 하나의 Trade를 가진다 (OneToOne)

Post는 여러 개의 Favorite, ChatRoom과 연관된다 (단방향)

#### 2-3. 기능 요구사항

##### 게시글 등록
- 판매자는 제목, 내용, 가격, 이미지, 카테고리, 거래 장소를 입력해 게시글을 등록한다
- 등록 시 최소 1장, 최대 10장의 이미지를 첨부할 수 있다
- 이미지 업로드 시 등록 순서를 기억하여 사용자가 설정한 순서대로 보여줘야 한다
- 대표 이미지는 목록의 첫 번째 이미지로 간주한다
- 가격은 Money라는 Embeddable 타입으로 관리할 수 있게 한다

##### 게시글 조회
- 게시글 열람 시 조회수는 1씩 증가한다 (중복 방지 캐싱 로직 구현 필요)

##### 게시글 수정
- 제목/내용, 이미지 순서 변경, 가격, 거래 장소를 변경할 수 있다
- 거래 완료 이후 게시글 정보를 수정할 수 없다
- 숨김 여부를 명시적으로 관리해야 한다

##### 게시글 삭제
- Soft Delete를 적용하지 않고 실제로 삭제한다

##### 게시글 상태 변경
- 판매중 -> 예약중 -> 거래완료로 변경할 수 있다
- 예약중으로 상태 변경 시 거래 대상을 지정해야 한다
- 거래 대상은 해당 게시글과 연관된 채팅방의 사용자만 지정할 수 있다
- 예약 대상을 게시글 작성자로 지정할 수 없다
- 예약중으로 상태 변경 시 거래 일시를 지정해야 한다

##### 게시글 검색/필터링
- 키워드(제목, 내용), 카테고리, 위치 기반으로 검색할 수 있다
- 최신순, 가격순, 거리순 등의 정렬을 지원한다

### 3. Post 엔티티 및 VO 구현

```java
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    private static final int PRODUCT_IMAGE_MAXIMUM = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Column(nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
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
    @Column(name = "image_url")
    private List<ProductImage> productImages;

    @Embedded
    private Location tradeLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus beforeHiddenStatus;

    @Embedded
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

        if (!StringUtils.hasText(tradeLocation.getDetail())) {
            throw new IllegalArgumentException("거래 장소의 상세 위치를 입력해주세요");
        }

        this.writer = user;
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
        if (!this.status.equals(PostStatus.ON_SALE)) {
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
        if (status.equals(PostStatus.RESERVED)) {
            throw new IllegalStateException("예약된 게시글은 숨길 수 없습니다");
        }

        this.beforeHiddenStatus = this.status;
        this.status = PostStatus.HIDDEN;
    }

    public void unhide() {
        this.status = beforeHiddenStatus;
    }

    public void sold() {
        if (!status.equals(PostStatus.RESERVED)) {
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
        this.title = title;
        this.content = content;
        this.category = category;
        this.productImages = new ArrayList<>(productImages);
        this.salesAmount = price;
        this.tradeLocation = tradeLocation;
    }

}
```

```java
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
```

```java
public enum PostStatus {

    ON_SALE,
    HIDDEN,
    RESERVED,
    SOLD

}
```

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewCount {

    @Column(name = "view_count", nullable = false)
    private int count;

    public void increase() {
        this.count++;
    }
}
```

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    @Column(nullable = false)
    private int value;

    public Money(int value) {
        if (value < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        this.value = value;
    }

}
```


## Favorite

### 1. 기능 명세
- 사용자는 게시글에 관심을 등록할 수 있다
- 사용자는 이미 등록한 관심을 해제할 수 있다
- 사용자는 내가 관심 등록한 게시글 목록을 조회할 수 있다
- 게시글 상세 페이지에서 관심 등록 여부를 확인할 수 있다
- 관심 수는 게시글마다 조회할 수 있다

### 2. 도메인 요구사항

#### 2-1. 주요 책임
- 사용자의 관심 등록 및 해제 관리
- 관심 등록 여부 판단
- 게시글의 전체 관심수 카운팅
- 관심 상품 목록 조회 기능 제공

#### 2-2. 도메인 모델 및 연관 관계 설정

Favorite: 관심을 등록한 사용자와 게시글 참조를 가지고 있는 모델

Favorite은 User와 Post 간의 다대다(ManyToMany)를 단방향 매핑으로 명확하게 관리한다

Favorite은 User 1명과 Post 1개를 연관짓는 단일 행 엔티티이다

Post의 관심 수는 별도 컬럼으로 캐싱할 수 있다 (쿼리 성능 개선 목적)

#### 2-3. 기능 요구사항

##### 관심 등록
- 사용자는 하나의 게시글에 대해 한 번만 관심 등록할 수 있다
- 동일 게시글에 대해 중복으로 관심 등록할 수 없다 (유니크 제약 필요)
- 관심 등록 시 등록 시간을 기록해야 한다 (저장 시간 기준으로 기록)

##### 관심 해제
- 사용자는 자신이 등록한 관심을 해제할 수 있다
- 관심 등록이 존재할 경우에만 삭제할 수 있다
- 해제는 데이터 삭제를 통해 이뤄진다

##### 관심 목록 조회
- 사용자는 자신이 등록한 관심 게시글 목록을 최신순으로 페이징 제공한다
- 게시글 제목, 이미지, 가격, 상태 등 필수 정보를 포함한다

##### 관심수 조회
- 쿼리로 count(*) 조회할 수 있지만 실시간 반응성이 필요한 경우 별도 캐시 컬럼(Post.favoriteCount) 사용을 고려한다
- 캐시 컬럼을 사용하는 경우 트랜잭션 간 동시성 이슈 해결 전략이 필요하다 (락, 이벤트 기반 async/sync 등)

##### 관심 여부 확인
- 특정 사용자와 게시글에 관심이 등록되어 있는지 Boolean 반환
- 게시글 상세 화면에 활용된다

### 3. Favorite 엔티티 구현

User 아이디와 Post 아이디를 기반으로 유니크 제약(@UniqueConstraint)을 걸어서 동일 게시글에 대해 중복으로 관심 등록하는 것을 방지한다

조회 성능 향상을 위해 User 아이디와 Post 아이디에 각각 인덱스를 설정한다

```java
@Entity
@Table(
        name = "favorites",

        // 사용자와 게시글 간 중복 찜 등록 방지
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_favorite_user_post", columnNames = {"user_id", "post_id"})
        },

        // 관심 게시글 목록, 게시글별 관심 통계 조회 시 성능 향상
        indexes = {
                @Index(name = "idx_favorite_user", columnList = "user_id"),
                @Index(name = "idx_favorite_post", columnList = "post_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public Favorite(User user, Post post) {
        this.user = user;
        this.post = post;
    }

}
```


## Category

### 1. 기능 명세
- 카테고리를 등록, 삭제할 수 있다
- 사용자는 카테고리를 선택해서 게시글을 등록할 수 있다
- 사용자는 특정 카테고리에 속한 게시글만 필터링하여 조회할 수 있다

### 2. 도메인 요구사항

#### 2-1. 주요 책임
- 게시글 분류를 위한 카테고리 구조 관리
- 게시글과의 관계 설정
- 카테고리 목록 조회 및 순서 유지

#### 2-2. 도메인 모델 및 연관 관계 설정

Category: 카테고리 정보를 포함하는 모델

Category와 Post는 1:N 단방향 (Post.category로 조회)

#### 2-3. 기능 요구사항

##### 카테고리 등록
- 카테고리 이름, 정렬 순서, 활성화 여부를 설정한다
- 카테고리의 이름은 중복될 수 없으며 최대 50자의 길이 제한을 가진다

##### 카테고리 수정
- 이름, 정렬 순서, 활성화 여부를 변경할 수 있다

##### 카테고리 삭제
- 해당 카테고리를 참조하는 게시글이 존재하면 삭제할 수 없다 (제약 조건 설정 또는 연관 제거 필요)
    - 활성화 여부로 제어하여 소프트 삭제 방식을 지원한다

##### 카테고리 조회
- 사용자에게 카테고리를 조회할 때 활성화된 카테고리만 정렬 순서대로 표시한다
- 특정 카테고리에 속한 게시글들을 필터링하여 조회할 수 있다
- 상위 카테고리를 기준으로 하여 하위 카테고리 리스트를 조회할 수 있다

### 3. Category 엔티티 구현

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    // displayOrder 순으로 자식 카테고리 정렬
    @OrderBy("displayOrder ASC")
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private boolean enabled;

    @Builder
    public Category(Category parent, String name, int displayOrder) {
        this.parent = parent;
        this.name = name;
        this.displayOrder = displayOrder;
        this.enabled = true;
    }

    public void addChild(Category child) {
        this.children.add(child);
        child.parent = this;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeDisplayOrder(int order) {
        this.displayOrder = order;
    }

    public boolean disable() {
        return enabled = false;
    }

    public boolean activate() {
        return enabled = true;
    }

    public boolean isRootCategory() {
        return parent == null;
    }

}
```


## Review

Review는 거래에 참여한 당사자 간 별점과 후기를 남길 수 있는 기능을 제공한다

### 1. 기능 명세
- 게시글의 상태가 판매완료인 경우 판매자와 구매자는 각각 리뷰를 남길 수 있다
- 판매자와 구매자는 거래 내역을 통해 자신과 상대방이 남긴 리뷰를 조회할 수 있다
- 리뷰는 수정 및 삭제할 수 없다

### 2. 도메인 요구사항

#### 2-1. 주요 책임
- 거래 완료 후 리뷰 작성 기능 연결
- 사용자 간 권한 검증 (작성자, 참여자 여부)
- 리뷰 목록 조회 지원

#### 2-2. 도메인 모델 및 연관 관계 설정

Review: 거래 리뷰와 관련한 정보와 리뷰에 참여하는 사용자의 참조를 포함하는 모델

Review와 Post는 N:1 단방향 관계를 가진다 (판매자 및 구매자 모두 작성 가능)

Review와 User는 N:1 단방향 관계를 가진다 (리뷰 작성자와 리뷰 대상자)

#### 2-3. 기능 요구사항

##### 리뷰 작성
- 게시글이 SOLD 상태일 때만 구매자와 판매자 모두 상대방에게 리뷰를 남길 수 있다
- 리뷰 작성 전 게시글이 삭제되면 서로 리뷰를 남길 수 없다
- 리뷰는 판매자와 구매자 각각 한 번만 작성할 수 있다
- 리뷰는 별점과 후기(1000자)를 포함한다

##### 리뷰 목록 조회
- 사용자는 자신에게 남겨진 리뷰 목록을 최신순으로 조회할 수 있다
- 리뷰 별점과 후기를 포함한다

##### 리뷰 관리
- 계정을 삭제하더라도 거래 리뷰는 유지된다

### 3. Review 엔티티 및 VO 구현

```java
@Entity
@Table(

        // 사용자 간 중복 리뷰 등록 방지
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_review_reviewer_reviewee_post", columnNames = {"reviewer_id", "reviewee_id", "post_id"})
        },

        // 리뷰어 통계 조회 시 성능 향상
        indexes = {
                @Index(name = "idx_review_reviewer", columnList = "reviewer_id"),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 탈퇴할 수 있으므로 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // 사용자가 탈퇴할 수 있으므로 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    // 게시글이 삭제될 수 있으므로 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Embedded
    @Column(nullable = false)
    private ReviewRating rating;

    @Column(length = 1000)
    private String comment;

    public static void validateReviewer(Review review, Long reviewerId) {
        if (!review.getReviewer().getId().equals(reviewerId))
            throw new IllegalArgumentException("리뷰어가 일치하지 않습니다.");
    }

}
```

```java
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
```


## Chat

### 1. 기능 명세
- 내가 참여한 채팅방 목록을 확인할 수 있다
- 상품에 대해 다른 사용자와 1:1 채팅방을 생성할 수 있다
- 채팅방을 나갈 수 있다
- 텍스트를 전송할 수 있다
- 채팅방의 전송한 메시지를 페이징 방식으로 조회할 수 있다
- 사용자가 읽지 않은 메시지 수를 확인하고, 읽음 처리할 수 있다
- 키워드를 포함하는 메시지를 채팅방 내에서 검색할 수 있다

### 2. 도메인 요구사항

#### 2-1. 주요 책임
- 채팅방 목록 조회
- 채팅방 관리
- 메시지 관리/검색
- 메시지 읽음 여부 표시

#### 2-2. 도메인 모델 및 연관 관계 설정

ChatMessage: 채팅 메시지 및 채팅방, 발신자 정보를 포함하는 모델

ChatRoom: 채팅방 이름, 참여자, 읽기 전용 상태를 관리하는 모델

ChatParticipant: 채팅방, 참여자, 마지막으로 읽은 메시지 참조 값을 보관하는 모델

ChatMessage는 ChatRoom과 N:1 단방향 관계를 가진다

ChatMessage는 User와 N:1 단방향 관계를 가진다

ChatRoom은 Post와 N:1 단방향 관계를 가진다

ChatRoom은 ChatParticipant와 1:N 양방향 관계를 가진다

ChatRoom은 ChatMessage와 1:N 양방향 관계를 가진다

ChatParticipant는 User와 N:1 단방향 관계를 가진다

#### 2-3. 기능 요구사항

##### 채팅방 관련
- 사용자는 특정 게시글에 대해 1개의 채팅방만 생성할 수 있다
- 판매자 또는 구매자만 채팅에 참여할 수 있다 (1:1)
- 채팅방은 게시글을 기준으로 식별되며 판매자와 구매자의 조합이 유일하다
- 사용자가 게시글을 삭제하거나 거래가 완료되어도 채팅방은 유지된다
- 채팅방에 연관된 사용자가 탈퇴하면 채팅방은 비활성화되며 읽기 상태 전용으로 전환된다
- 채팅방 삭제 시 관련된 모든 데이터도 함께 삭제한다

##### 메시지 관련
- 메시지는 채팅방에 속하며 발신자와 보낸 시각, 내용을 포함한다
- 메시지는 발신 순서대로 정렬되며 텍스트만 지원한다
- 읽음 상태 기능을 지원한다

### 3. ChatMessage, ChatRoom, ChatParticipant 엔티티 구현

```java
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

}
```

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private boolean isReadOnly = false;

    public ChatRoom(Post post, User user1, User user2) {
        this.post = post;
        ChatParticipant participant1 = new ChatParticipant(this, user1);
        ChatParticipant participant2 = new ChatParticipant(this, user2);
        participants.addAll(List.of(participant1, participant2));
    }

    public void deactivate() {
        isReadOnly = true;
    }

}
```

```java
@Entity
@Table(
        uniqueConstraints = {
                // 사용자 간 채팅방 중복 참여 방지 유니크 제약조건
                @UniqueConstraint(name = "uc_chat_participant_chat_room_user", columnNames = {"chat_room_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 채팅방 메시지 읽음 여부를 표시하기 위해 마지막으로 읽은 메시지 시각 기록
    private LocalDateTime lastReadMessageAt;

    // 채팅방 재입장 시 기존 대화 메시지 내용 이후부터 읽을 수 있도록 재입장 시각 기록
    private LocalDateTime rejoinedAt;

    private boolean isLeaved = false;

    public ChatParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public void readMessage() {
        if (isLeaved) throw new IllegalArgumentException("채팅방을 나간 사용자는 메시지를 읽을 수 없습니다");

        this.lastReadMessageAt = LocalDateTime.now();
    }

    public void leaveChatRoom() {
        this.isLeaved = true;
    }

    public void rejoinChatRoomIfLeaved() {
        if (isLeaved) {
            this.isLeaved = false;
            this.rejoinedAt = LocalDateTime.now();
        }
    }

}
```