package db.ninja.post.service;


import db.ninja.category.Category;
import db.ninja.category.CategoryService;
import db.ninja.common.vo.Location;
import db.ninja.post.vo.Money;
import db.ninja.post.entity.Post;
import db.ninja.post.entity.ProductImage;
import db.ninja.post.repository.PostRepository;
import db.ninja.user.User;
import db.ninja.user.UserQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SequencedMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
        Category category = categoryService.getCategoryById(categoryId);

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
