package db.ninja.post.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import db.ninja.favorite.QFavorite;
import db.ninja.post.dto.PostSearchCondition;
import db.ninja.post.dto.PostWithFavoriteCountDto;
import db.ninja.post.entity.Post;
import db.ninja.post.entity.QPost;
import db.ninja.user.QUser;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


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
