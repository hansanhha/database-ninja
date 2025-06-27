[이전으로](../README.md)

[예제 프로젝트 1 - 요구사항 정리 및 JPA 엔티티 설계](./예제%20프로젝트%201%20-%20요구사항%20정리%20및%20JPA%20엔티티%20설계.md)

[기능 구현 학습 포인트](#기능-구현-학습-포인트)

[User 기능 구현: 기본 CRUD, Soft Delete](#user-기능-구현-기본-crud-soft-delete)

[Post 기능 구현: EmbeddedCollection, 동적 필터링/정렬, 페이징, DTO 프로젝션](#post-기능-구현-embeddedcollection-동적-필터링정렬-페이징-dto-프로젝션)

[Favorite 기능 구현](#favorite-기능-구현)

[Category 기능 구현](#category-기능-구현)

[Review 기능 구현: 유니크 제약 활용](#review-기능-구현-유니크-제약-활용)

[Chat 기능 구현](#chat-기능-구현)


## 기능 구현 학습 포인트

### User 도메인

#### 학습 포인트
- 엔티티 생성/삭제/조회 기본 기능
- Soft Delete 처리 방식 (enum 값 변경)
- 도메인 로직을 엔티티에 넣는 방식 (닉네임 변경 제한 등)

#### 관련 기술
- 도메인 메서드와 유효성 검증 (비즈니스 로직 vs 프레젠테이션 검증)

### Post 도메인

#### 학습 포인트
- 게시글 동적 조회: 카테고리, 제목, 거래 상태, 가격 조건 등
- 이미지 리스트, 정렬 순서 유지 (1:N Embedded VO)
- 거래 상태 업데이트 (도메인 로직 + 검증)

#### 관련 기술
- QueryDSL 동적 조건 (BooleanBuilder, whereIf)
- Embedded 컬렉션 처리

### Favorite 도메인

#### 학습 포인트
- 관심 등록 여부 확인 (exists, count)

#### 관련 기술
- 복합 키 or 유니크 제약 (@UniqueConstraint)
- 단순한 쿼리 성능 최적화 (exists vs left join)

### Category 도메인

#### 학습 포인트
- 정적 데이터 관리 (enum vs 엔티티 설계 판단)
- 전체 목록 조회 (캐싱 고려 여부)

#### 관련 기술
- 애플리케이션 시작 시 조회용 데이터 캐싱
- enum-like 엔티티 관리 전략

### Trade 도메인

#### 학습 포인트
- 리뷰 작성 여부 검증
- 리뷰 평균값 조회

#### 관련 기술
- OneToOne 연관 관계와 조인 전략
- @Embedded 값 객체 비교
- 평균 평점 구하기 (group by, avg, QueryDSL DTO 프로젝션)

### Chat 도메인

#### 학습 포인트
- 채팅방 목록 조회 (참여자 기준)
- 채팅 메시지 조회 (읽지 않은 메시지 수, 읽음 여부)
- 마지막 메시지 기준 정렬, 페이징 처리

#### 관련 기술
- @OneToMany(mappedBy = ...), @OrderBy
- 서브쿼리 활용 (lastMessage, unreadCount)
- 페치 조인으로 N+1 문제 해결

### 기능 구현 전 확인할 사항

#### 연관 관계 방향
- 기본적으로 단방향으로 설계하되 꼭 필요한 경우만 양방향 연관 관계를 사용
- 양방향이면 항상 mappedBy, 연관 관계 편의 메서드를 사용

#### 지연 로딩 vs 즉시 로딩
- 기본적으로 지연 로딩 (FetchType.LAZY)
- 목록 조회 시 페치 조인 고려
- 즉시 로딩은 거의 사용하지 않도록 습관화

#### 조회 DTO와 응답 모델 구분
- 엔티티 -> DTO 변환 단계 분리 (QueryDSL 프로젝션)
- 반환 필드가 명확하면 ConstructorExpression or Projections.fields

#### 쿼리 성능
- join fetch vs select in vs 서브 쿼리 성능 고려
- 페이징 쿼리에선 페치 조인 주의 (count 쿼리 왜곡 이슈)

#### 유효성 검증 책임 위치
- 컨트롤러 vs 서비스 vs 도메인 모델(JPA 엔티티)
- e.g) 닉네임 형식 -> 컨트롤러, 변경 주기 검증 -> 도메인 모델

#### 기능 구현 시 체크하면 좋을 것들
- 무엇을 기준으로 쿼리할 것인가 (정렬/필터 조건)?
- 반환 데이터가 무엇인가 (DTO 프로젝션)?
- 성능 상 이슈가 발생할 가능성이 있는지?
- 도메인 규칙은 어느 계층에 있어야 하는지?


## User 기능 구현: 기본 CRUD, Soft Delete

User 기능은 엔티티 생성, 조회, 삭제, 닉네임 변경 등의 기본적인 CRUD 기능을 포함한다

User 기능 구현 포인트는 다음과 같다
- 서비스 계층에서 비즈니스 로직을 처리하지 않고 엔티티 메서드로 도메인 로직을 구현하는 방식을 사용한다
- User 엔티티 삭제 시 Soft Delete 방식으로 상태값을 변경하여 삭제 처리한다
- User 엔티티 또는 프로필 조회를 위한 별도의 쿼리 서비스 객체를 구현한다

### UserService: User 엔티티 생성/수정/삭제 서비스 객체 구현

```java
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signUp(String username, String password, String nickname, Location location) {
        validateDuplicateUsername(username);
        validateDuplicateNickname(nickname);

        User user = new User(username, password, nickname, location);
        userRepository.save(user);
    }

    public AuthenticationToken signIn(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다"));

        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다");
        }

        return new AuthenticationToken(user.getUsername());
    }

    // AuthenticationToken은 사용자 인증 정보를 담고 있는 객체로 가정한다
    public void updatePassword(AuthenticationToken token, String newPassword) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.changePassword(newPassword);
    }

    public void deleteUser(AuthenticationToken token) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.delete();
    }

    public void updateUserNickname(AuthenticationToken token, String newNickname) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        validateDuplicateNickname(newNickname);
        user.changeNickname(newNickname);
    }

    public void addLocation(AuthenticationToken token, Location location) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.addLocation(location);
    }

    public void removeLocation(AuthenticationToken token, Location location) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.removeLocation(location);
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
        }
    }

}
```

### UserQueryService: User 엔티티 및 프로필(DTO) 조회용 서비스 객체

```java
@Service
// 읽기 전용 트랜잭션 적용
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryServiceImpl {

    private final UserRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public UserProfile getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return new UserProfile(user.getUsername(), user.getNickname(), user.getLocations().getFirst(), user.getCreatedAt());
    }

}
```

### UserRepository: User 엔티티 CrudRepository 구현

사용자 기능은 간단한 CRUD 기능이므로 쿼리 메서드만을 이용한다

```java
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByNickname(String nickname);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

}
```


## Post 기능 구현: EmbeddedCollection, 동적 필터링/정렬, 페이징, DTO 프로젝션

Post 기능은 중고 상품 게시글의 생성, 수정, 삭제, 조회, 상태 변경 등의 기능을 포함한다

Post 기능 구현 포인트는 다음과 같다
- 게시글 상태 변경, 수정 시 작성자가 맞는지 검증한다
- 게시글 조회 시 카테고리, 제목, 거래 상태, 가격 조건 등으로 동적 필터링을 지원한다
- 게시글 이미지 리스트는 1:N Embedded VO로 관리하며, 정렬 순서를 유지한다
- 사용자가 자신이 등록한 관심 게시글을 조회할 수 있도록 한다

관심 등록은 Favorite 기능이나 사용자가 관심 등록한 게시글 조회 기능 자체는 게시글 정보가 중심이므로 Post 도메인에서 구현하는 것으로 한다 (관심 등록 여부는 조회 조건일 뿐이지 주도적 엔티티에 포함되지 않기 때문)

### PostService: Post 엔티티 생성/수정/삭제 서비스 객체 구현

PostService 객체에서 제공하는 기능은 다음과 같다
- 게시글 생성, 수정, 삭제
- 게시글 상태 변경 (예약, 거래 완료, 거래 취소, 숨기기/숨김 해제)

```java
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserQueryService userQueryService;
    private final CategoryService categoryService;

    // ======== Post 엔티티 생성/수정/삭제 메서드 ========

    public Long createPost(Long userId, String title, String content, int amount, Long categoryId, Location tradeLocation, SequencedMap<String, String> uploadImages) {
        User user = userQueryService.getUserById(userId);
        Category category = categoryService.getCategoryById(categoryId);

        List<ProductImage> productImages = convertToProductImages(uploadImages);

        Post post = Post.builder()
                .user(user)
                .title(title)
                .category(category)
                .content(content)
                .salesAmount(new Money(amount))
                .productImages(productImages)
                .build();

        Post save = postRepository.save(post);
        return save.getId();
    }

    public void updatePost(Long userId, Long postId, String title, String content, int amount, Long categoryId, Location tradeLocation, SequencedMap<String, String> uploadImages) {
        Post post = postRepository.findByIdWithAuthor(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);
        Category category = categoryQueryService.getCategoryById(categoryId);

        post.validateAuthor(user);

        List<ProductImage> productImages = convertToProductImages(uploadImages);
        post.update(title, content, category, productImages, new Money(amount), tradeLocation);
    }

    public void updateTradeDate(Long userId, Long postId, LocalDateTime tradeDate) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);

        post.validateAuthor(user);
        post.updateTradeDate(tradeDate);
    }

    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);

        post.validateAuthor(user);

        postRepository.delete(post);
    }

    // ======== Post 엔티티 상태 변경 메서드 ========

    public void reserveTrade(Long userId, Long postId, Long targetUserId, LocalDateTime reserveDate) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);
        User tradePartner = userQueryService.getUserById(targetUserId);

        post.validateAuthor(user);
        post.reserve(tradePartner, reserveDate);
    }

    public void completeTrade(Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);

        post.validateAuthor(user);
        post.sold();
    }

    public void cancelTrade(Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);

        post.validateAuthor(user);
        post.cancelReservation();
    }

    public void hidePost(Long userId, Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);

        post.validateAuthor(user);
        post.hide();
    }

    public void unhidePost(Long userId, Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId).orElseThrow();
        User user = userQueryService.getUserById(userId);

        post.validateAuthor(user);
        post.unhide();
    }

    private List<ProductImage> convertToProductImages(SequencedMap<String, String> uploadImages) {
        return uploadImages.entrySet().stream()
                .map(entry -> new ProductImage(entry.getKey(), entry.getValue()))
                .toList();
    }

}
```

### PostQueryService: Post 엔티티 및 게시글 목록 조회용 서비스 객체 구현

게시글 조회 기능은 다음과 같다
- 게시글 ID로 단건 조회 (게시글 상세 조회) 및 조회수 증가
- 게시글 목록 조회
    - 동적 필터링: 카테고리, 제목, 거래 상태, 가격 조건, 위치
    - 정렬: 최신순, 가격순, 거리순
    - 페이징
- 사용자의 관심 게시물 목록 조회

게시글 상세 조회 시 조회수를 증가시키고, 사용자가 로그인한 경우에만 관심 여부를 확인한다

```java
@Service
@Transactional
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final FavoriteService favoriteService;

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow();
    }

    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        PostWithFavoriteCountDto postDetailDto = postRepository.findPostDetailByIdFetchJoin(postId).orElseThrow();
        Post post = postDetailDto.post();

        post.increaseViewCount();

        // 사용자가 로그인한 경우에만 관심 여부를 확인한다
        boolean isFavorite = false;
        if (userId != null) isFavorite = favoriteService.isFavoritePost(userId, postId);

        return new PostDetailResponse(post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getSalesAmount(),
                post.getStatus().getDisplayName(),
                post.getTradeLocation(),
                post.getProductImages().stream().map(ProductImage::getUrl).toList(),
                post.getViewCount().getValue(),
                postDetailDto.favoriteCount(),
                isFavorite,
                post.getCreatedAt());
    }

    public Slice<PostSummaryResponse> getPosts(PostSearchCondition condition, Pageable pageable) {
        Slice<PostWithFavoriteCountDto> postSummaryDtos = postRepository.searchPostSummaryByCondition(condition, pageable);
        return convertToPostSummaryResponse(postSummaryDtos);
    }

    public Slice<PostSummaryResponse> getMyFavoritePosts(Long userId, Pageable pageable) {
        Slice<PostWithFavoriteCountDto> favoritePostDtos = postRepository.findFavoritePostsByUserId(userId, pageable);
        return convertToPostSummaryResponse(favoritePostDtos);
    }

    private Slice<PostSummaryResponse> convertToPostSummaryResponse(Slice<PostWithFavoriteCountDto> postDtos) {
        return postDtos.map(dto -> {
            Post post = dto.post();
            return new PostSummaryResponse(post.getId(),
                    post.getTitle(),
                    dto.thumbnailUrl(),
                    post.getTradeLocation(),
                    post.getStatus().getDisplayName(),
                    post.getSalesAmount(),
                    post.getViewCount().getValue(),
                    dto.favoriteCount());
        });
    }

}
```

### PostCustomRepositoryImpl: Post 엔티티 동적 조회를 위한 QueryDSL 활용

findPostDetailByIdFetchJoin 메서드는 게시글 상세 조회를 위한 메서드로 다음과 같은 연관 엔티티(ManyToOne)와 관심수 합계를 구한다
- 작성자(User)
- 카테고리(Category)
- 게시글 이미지(ProductImage)
- 관심수(Favorite)

searchPostSummaryByConditionAndPageable 메서드는 게시글 목록 조회를 위한 메서드로 다음과 같은 조건을 지원한다
- 카테고리
- 제목
- 거래 상태
- 최소/최대 가격 조건

또한 페이징 처리와 정렬을 지원하며, 각 게시글의 관심 수를 함께 조회한다

동적 정렬은 Pageable 객체의 Sort.Order 정보를 기반으로 PathBuilder를 이용하여 OrderSpecifier 배열로 변환한 후 QueryDSL 쿼리에 적용된다

```java
@Repository
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory query;
    private final QUser user = QUser.user;
    private final QPost post = QPost.post;
    private final QFavorite favorite = QFavorite.favorite;

    /**
     * <ul>게시글 상세 조회</ul>
     * <ol>페치 조인: 작성자, 카테고리, 게시글 이미지</ol>
     * <ol>관심수 합계</ol>
     */
    @Override
    public Optional<PostWithFavoriteCountDto> findPostDetailByIdFetchJoin(Long postId) {
        return Optional.ofNullable(
                query
                        .select(postWithFavoriteCountDtoProjections(null, favorite.post.id.eq(postId)))
                        .from(post)
                        .where(post.id.eq(postId))
                        .leftJoin(post.author).fetchJoin()
                        .leftJoin(post.category).fetchJoin()
                        .leftJoin(post.productImages).fetchJoin()
                        .fetchOne()
        );
    }

    /**
     * <ul>게시글 검색 조건에 맞는 게시글 목록 조회 (페치조인 없음)</ul>
     * <ol>첫 번째 상품 이미지</ol>
     * <ol>페이징 처리</ol>
     * <ol>동적 조건 및 정렬 적용</ol>
     * <ol>관심수 합계</ol>
     */
    @Override
    public Slice<PostWithFavoriteCountDto> searchPostSummaryByCondition(PostSearchCondition condition, Pageable pageable) {
        List<PostWithFavoriteCountDto> result = query
                .select(postWithFavoriteCountDtoProjections(post.productImages.get(0).url, favorite.post.id.eq(post.id)))
                .from(post)
                .where(buildPostSearchCondition(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // hasNext 여부를 판단하기 위해 +1
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.removeLast();
        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Override
    public Slice<PostWithFavoriteCountDto> findFavoritePostsByUserId(Long userId, Pageable pageable) {
        List<PostWithFavoriteCountDto> result = query
                .select(postWithFavoriteCountDtoProjections(post.productImages.get(0).url, favorite.post.id.eq(post.id)))
                .from(post)
                .join(favorite.post).on(favorite.post.id.eq(post.id))
                .where(user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // hasNext 여부를 판단하기 위해 +1
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.removeLast();
        return new SliceImpl<>(result, pageable, hasNext);
    }

    /**
     * 게시글 조회용 DTO 프로젝션 생성 메서드
     * @param thumbnailImage 상세 조회 시 null 전달, 목록 조회 시 첫 번째 상품 이미지 URL 전달
     * @param FavoriteTotalCountConditionId 관심수 합계를 구하기 위한 조건
     */
    private ConstructorExpression<PostWithFavoriteCountDto> postWithFavoriteCountDtoProjections(StringPath thumbnailImage, BooleanExpression FavoriteTotalCountConditionId) {
        return Projections.constructor(PostWithFavoriteCountDto.class,
                post,
                thumbnailImage,
                JPAExpressions
                        .select(favorite.count())
                        .from(favorite)
                        .where(FavoriteTotalCountConditionId)
        );
    }

    private BooleanBuilder buildPostSearchCondition(PostSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (condition.categoryId() != null) {
            builder.and(post.category.id.eq(condition.categoryId()));
        }
        if (StringUtils.hasText(condition.title())) {
            builder.and(post.title.containsIgnoreCase(condition.title()));
        }
        if (condition.status() != null) {
            builder.and(post.status.eq(condition.status()));
        }
        if (condition.minPrice() > 0) {
            builder.and(post.salesAmount.count().goe(condition.minPrice()));
        }
        if (condition.maxPrice() > 0) {
            builder.and(post.salesAmount.count().loe(condition.maxPrice()));
        }
        return builder;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        return sort.stream()
                .map(this::toOrderSpecifier)
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        PathBuilder<Post> postPathBuilder = new PathBuilder<>(Post.class, "post");

        return new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                postPathBuilder.get(order.getProperty(), Comparable.class));
    }

}
```

## Favorite 기능 구현

Favorite 기능은 사용자가 게시글에 관심을 등록하거나 해제하는 기능, 관심 등록한 게시글인지 확인하는 기능을 포함한다

다만 관심 등록한 게시물 목록 조회 기능은 게시글 정보가 중심이므로 Post 도메인에서 구현한다 [관심 등록한 게시물 목록 조회](#postqueryservice-post-엔티티-및-게시글-목록-조회용-서비스-객체-구현)

### FavoriteService: 관심 등록/해제 및 관심 등록 여부 확인 서비스 객체 구현

```java
@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserQueryService userQueryService;
    private final PostQueryService postQueryService;

    public void addFavorite(Long userId, Long postId) {
        if (favoriteRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalArgumentException("이미 관심 등록한 게시글입니다.");
        }

        User user = userQueryService.getUserById(userId);
        Post post = postQueryService.getPostById(postId);

        Favorite favorite = new Favorite(user, post);
        favoriteRepository.save(favorite);
    }

    public void removeFavorite(Long favoriteId) {
        favoriteRepository.deleteById(favoriteId);
    }

    public boolean isFavoritePost(Long userId, Long postId) {
        return favoriteRepository.existsByUserIdAndPostId(userId, postId);
    }

}
```

### FavoriteRepository 구현

```java
public interface FavoriteRepository extends CrudRepository<Favorite, Long> {

    @Query("SELECT exists(f) " +
            "FROM Favorite f " +
            "WHERE f.user.id = :userId AND f.post.id = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

}
```


## Category 기능 구현

카테고리는 비교적 간단한 기능으로 카테고리 조회와 생성 기능을 구현한다

```java
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Long createCategory(Long parentId, String name, int displayOrder) {
        Category parent = null;

        if (parentId != null) parent = categoryRepository.findById(parentId).orElseThrow();

        Category category = categoryRepository.save(new Category(parent, name, displayOrder));
        return category.getId();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

}
```

## Review 기능 구현: 유니크 제약 활용

리뷰 기능은 사용자가 거래 후 상대방에게 리뷰를 작성하는 기능을 포함한다

동일한 리뷰어가 여러 번 리뷰를 작성할 수 없도록 Review 엔티티에 리뷰어, 리뷰이, 거래 게시글에 대한 유니크 제약을 추가해놓았다

```java
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostQueryService postQueryService;
    private final UserQueryService userQueryService;

    public Long createReview(Long userId, Long targetUserId, Long tradePostId, String comment, int rating) {
        User reviewer = userQueryService.getUserById(userId);
        User reviewee = userQueryService.getUserById(targetUserId);
        Post post = postQueryService.getPostById(tradePostId);

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .comment(comment)
                .rating(new ReviewRating(rating))
                .post(post)
                .build();

        Review save = reviewRepository.save(review);
        return save.getId();
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findByIdFetchJoinReviewer(reviewId).orElseThrow();
        Review.validateReviewer(review, userId);
        reviewRepository.delete(review);
    }

    public Slice<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        return reviewRepository.findAllByReviewerId(userId, pageable);
    }

}
```

### ReviewCustomRepositoryImpl: Review 엔티티 동적 조회를 위한 QueryDSL 활용

```java
@Override
public Slice<ReviewResponse> findAllByReviewerId(Long reviewerId, Pageable pageable) {
    List<ReviewResponse> result = query
            .select(
                    Projections.constructor(ReviewResponse.class,
                            review.id,
                            review.reviewer.id,
                            review.reviewee.id,
                            review.post.id,
                            review.post.title,
                            review.post.productImages.get(0).url,
                            review.comment,
                            review.rating.value,
                            review.createdAt
                    )
            )
            .from(review)
            .leftJoin(review.reviewer).on(review.id.eq(review.reviewer.id))
            .leftJoin(review.reviewee).on(review.id.eq(review.reviewee.id))
            .leftJoin(review.post).on(review.id.eq(review.post.id))
            .where(review.reviewer.id.eq(reviewerId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1) // hasNext 여부를 판단하기 위해 +1
            .fetch();

    boolean hasNext = result.size() > pageable.getPageSize();
    if (hasNext) result.remove(pageable.getPageSize());
    return new SliceImpl<>(result, pageable, hasNext);
}
```


## Chat 기능 구현

Chat 기능은 특정 게시글을 기준으로 판매자와 구매자가 채팅을 할 수 있는 기능이다

Chat 기능 구현 포인트는 다음과 같다
- 채팅방 목록을 조회할 때 가장 최근 메시지를 기준으로 정렬한다
- 채팅방별로 읽지 않은 메시지 수를 함께 조회한다
- 채팅 메시지 조회 시 읽음 여부를 업데이트한다
- 채팅 메시지를 가장 최근 메시지 기준으로 정렬하고 페이징 처리한다
- 사용자가 키워드를 이용하여 채팅 메시지를 검색할 수 있도록 한다


### ChatService: 채팅방 생성, 메시지 전송, 채팅방 목록 조회 서비스 객체 구현

```java
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final UserQueryService userQueryService;
    private final PostQueryService postQueryService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public Long createChatRoom(Long postId, Long buyerId, Long sellerId) {
        Optional<ChatRoom> existingChatRoomOptional = chatRoomRepository.findByParticipantsIdFetchJoin(buyerId, sellerId);

        // 채팅방이 이미 존재하는 경우 채팅방을 다시 만들지 않으며 채팅방을 나간 참여자가 있다면 재입장시킨다
        if (existingChatRoomOptional.isPresent()) {
            ChatRoom existingChatRoom = existingChatRoomOptional.get();
            List<ChatParticipant> participants = existingChatRoom.getParticipants();
            participants.forEach(ChatParticipant::rejoinChatRoomIfLeaved);
            return existingChatRoom.getId();
        }

        Post post = postQueryService.getPostById(postId);
        List<User> users = userQueryService.getUsersByIds(List.of(buyerId, sellerId));
        ChatRoom chatRoom = new ChatRoom(post, users.get(0), users.get(1));

        ChatRoom save = chatRoomRepository.save(chatRoom);
        return save.getId();
    }

    public void leaveChatRoom(Long chatRoomId, Long userId) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(chatRoomId, userId).orElseThrow();
        chatParticipant.leaveChatRoom();
    }

    public void sendMessage(Long chatRoomId, Long senderId, String content) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndUserIdFetchJoin(chatRoomId, senderId).orElseThrow();
        ChatRoom chatRoom = chatParticipant.getChatRoom();
        User sender = chatParticipant.getUser();

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .build();

        chatMessageRepository.save(chatMessage);
    }

}
```

### ChatQueryService: 채팅방 목록 조회 및 채팅 메시지 조회/검색 서비스 객체 구현

```java
@Service
@Transactional
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomDetailResponse getChatRoomByPostId(Long userId, Long postId) {

        return chatRoomRepository.findChatRoomDetailByUserIdAndPostId(userId, postId).orElseThrow();
    }

    public ChatRoomDetailResponse getChatRoomByRoomId(Long userId, Long roomId) {
        return chatRoomRepository.findChatRoomDetailByUserIdAndRoomId(userId, roomId).orElseThrow();
    }

    public Slice<ChatRoomSummaryResponse> getChatRooms(Long userId, Pageable pageable) {
        return chatRoomRepository.findChatRoomSummaryAllByUserId(userId, pageable);
    }

    public Slice<ChatMessageResponse> getChatMessages(Long userId, Long roomId, LocalDateTime findBeforeDate) {
        return chatMessageRepository.findChatMessagesByRoomId(userId, roomId, findBeforeDate);
    }

    public Slice<ChatMessageResponse> searchMessages(Long userId, Long roomId, String keyword, Pageable pageable) {
        return chatMessageRepository.searchMessagesByRoomIdAndKeyword(userId, roomId, keyword, pageable);
    }

}
```

### ChatMessageQueryHelper: 채팅 메시지 조회를 위한 헬퍼 클래스

ChatRoomCustomRepositoryImpl 클래스와 ChatMessageCustomRepositoryImpl 클래스에서 공통적으로 사용되는 채팅 메시지 조회 로직을 분리하여 재사용성을 높인다

```java
@Component
@RequiredArgsConstructor
public class ChatMessageQueryHelper {

    private final JPAQueryFactory query;
    private final QChatMessage message = QChatMessage.chatMessage;

    public List<ChatMessageResponse> fetchMessagesByRoomId(Long roomId, @Nullable LocalDateTime fetchAfterDate, @Nullable LocalDateTime fetchBeforeDate, int limit) {
        return query
                .select(
                        Projections.constructor(ChatMessageResponse.class,
                                message.id,
                                message.chatRoom.id,
                                message.sender.id,
                                message.sender.nickname,
                                message.content,
                                message.createdAt)
                )
                .from(message)
                .where(
                        message.chatRoom.id.eq(roomId),
                        fetchAfterDate != null ? message.createdAt.after(fetchAfterDate) : null,
                        fetchBeforeDate != null ? message.createdAt.before(fetchBeforeDate) : null
                )
                .orderBy(message.createdAt.desc())
                .limit(limit)
                .fetch();
    }

}
```

### ChatRoomCustomRepositoryImpl: 채팅방 목록 및 메시지 조회를 위한 QueryDSL 활용

```java
@Repository
@RequiredArgsConstructor
public class ChatRoomCustomRepositoryImpl implements ChatRoomCustomRepository {

    private final ChatMessageQueryHelper messageQueryHelper;
    private final JPAQueryFactory query;
    private final QChatRoom room = QChatRoom.chatRoom;
    private final QChatParticipant participant = QChatParticipant.chatParticipant;
    private final QChatMessage message = QChatMessage.chatMessage;
    private final QPost post = QPost.post;

    @Override
    public Optional<ChatRoomDetailResponse> findChatRoomDetailByUserIdAndPostId(Long postId, Long userId) {
        return getChatRoomDetailByPredicates(room.post.id.eq(postId), participant.user.id.eq(userId));
    }

    @Override
    public Optional<ChatRoomDetailResponse> findChatRoomDetailByUserIdAndRoomId(Long roomId, Long userId) {
        return getChatRoomDetailByPredicates(room.id.eq(roomId), participant.user.id.eq(userId));
    }

    @Override
    public Slice<ChatRoomSummaryResponse> findChatRoomSummaryAllByUserId(Long userId, Pageable pageable) {
        QChatMessage latestMessage = new QChatMessage("latestMessage");

        SubQueryExpression<Long> latestMessageId = JPAExpressions
                .select(message.id)
                .from(message)
                .where(message.chatRoom.id.eq(room.id))
                .orderBy(message.createdAt.desc())
                .limit(1);

        List<ChatRoomSummaryResponse> result = query
                .select(Projections.constructor(ChatRoomSummaryResponse.class,
                        room.id,
                        room.post.productImages.get(0).url,
                        room.post.tradeLocation,
                        room.post.status,
                        room.post.title,
                        JPAExpressions.
                                select(message.sender.nickname)
                                .from(message)
                                .where(message.id.eq(latestMessageId)),
                        JPAExpressions.
                                select(message.content)
                                .from(message)
                                .where(message.id.eq(latestMessageId)))
                )
                .from(participant)
                .join(participant.chatRoom, room)
                .join(room.post, post)
                .where(participant.user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // hasNext 여부를 판단하기 위해 +1
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.removeLast();
        return new SliceImpl<>(result, pageable, hasNext);
    }

    private Optional<ChatRoomDetailResponse> getChatRoomDetailByPredicates(Predicate... predicates) {
        QChatParticipant userCP = new QChatParticipant("user");
        QChatParticipant partnerCP = new QChatParticipant("partner");

        Tuple result = findChatRoomDetail(predicates);
        if (result == null) return Optional.empty();

        LocalDateTime userRejoinedAt = result.get(userCP.rejoinedAt);
        List<ChatMessageResponse> chatMessageDtos = messageQueryHelper.fetchMessagesByRoomId(result.get(room.id), userRejoinedAt, LocalDateTime.now(), 20);

        return Optional.of(new ChatRoomDetailResponse(
                result.get(room.id),
                result.get(post.id),
                result.get(userCP.user.id),
                result.get(partnerCP.user.id),
                result.get(post.productImages.get(0).url),
                result.get(post.tradeLocation),
                result.get(post.status).getDisplayName(),
                result.get(post.title),
                result.get(userCP.user.nickname),
                result.get(partnerCP.user.nickname),
                result.get(userCP.lastReadMessageAt),
                chatMessageDtos,
                Boolean.TRUE.equals(result.get(room.isReadOnly))
        ));
    }

    private Tuple findChatRoomDetail(Predicate... predicates) {
        QChatParticipant userCP = new QChatParticipant("user");
        QChatParticipant partnerCP = new QChatParticipant("partner");

        return query
                .select(
                        room.id, post.id, userCP.user.id, partnerCP.user.id,
                        post.title, post.productImages.get(0).url,
                        userCP.user.nickname, partnerCP.user.nickname,
                        userCP.lastReadMessageAt, userCP.rejoinedAt, room.isReadOnly
                )
                .from(room)
                .join(room.post, post)
                .join(room.participants, userCP)
                .join(room.participants, partnerCP)
                .where(predicates)
                .fetchOne();
    }

}
```

### ChatMessageCustomRepositoryImpl: 채팅 메시지 조회를 위한 QueryDSL 활용

```java
@Repository
@RequiredArgsConstructor
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository {

    private final ChatMessageQueryHelper messageQueryHelper;
    private final JPAQueryFactory query;
    private final QChatMessage message = QChatMessage.chatMessage;
    private final QChatParticipant participant = QChatParticipant.chatParticipant;

    @Override
    public Slice<ChatMessageResponse> searchMessagesByRoomIdAndKeyword(Long userId, Long roomId, String keyword, Pageable pageable) {
        List<ChatMessageResponse> result = query
                .select(Projections.constructor(ChatMessageResponse.class,
                        message.id,
                        message.chatRoom.id,
                        message.sender.id,
                        message.sender.nickname,
                        message.content,
                        message.createdAt
                ))
                .from(message)
                .where(message.content.likeIgnoreCase(keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.removeLast();
        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Override
    public Slice<ChatMessageResponse> findChatMessagesByRoomId(Long userId, Long roomId, LocalDateTime findBeforeDate) {
        LocalDateTime rejoinedAt = JPAExpressions
                .select(participant.rejoinedAt)
                .from(participant)
                .where(participant.user.id.eq(userId), participant.chatRoom.id.eq(roomId))
                .fetchOne();

        List<ChatMessageResponse> chatMessageResponses = messageQueryHelper.fetchMessagesByRoomId(roomId, rejoinedAt, findBeforeDate, 21);
        boolean hasNext = chatMessageResponses.size() > 20;
        if (hasNext) chatMessageResponses.removeLast();
        return new SliceImpl<>(chatMessageResponses, Pageable.unpaged(), hasNext);
    }

}
```

