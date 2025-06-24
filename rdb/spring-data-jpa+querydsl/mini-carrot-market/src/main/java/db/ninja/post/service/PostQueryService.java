package db.ninja.post.service;


import db.ninja.favorite.FavoriteService;
import db.ninja.post.dto.PostDetailResponse;
import db.ninja.post.dto.PostSearchCondition;
import db.ninja.post.dto.PostSummaryResponse;
import db.ninja.post.dto.PostWithFavoriteCountDto;
import db.ninja.post.entity.Post;
import db.ninja.post.entity.ProductImage;
import db.ninja.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
