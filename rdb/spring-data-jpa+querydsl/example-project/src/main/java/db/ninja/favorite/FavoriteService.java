package db.ninja.favorite;


import db.ninja.post.entity.Post;
import db.ninja.post.repository.PostRepository;
import db.ninja.user.User;
import db.ninja.user.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserQueryService userQueryService;
    private final PostRepository postRepository;

    public void addFavorite(Long userId, Long postId) {
        if (favoriteRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalArgumentException("이미 관심 등록한 게시글입니다.");
        }

        User user = userQueryService.getUserById(userId);
        Post post = postRepository.findById(postId).orElseThrow();

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
