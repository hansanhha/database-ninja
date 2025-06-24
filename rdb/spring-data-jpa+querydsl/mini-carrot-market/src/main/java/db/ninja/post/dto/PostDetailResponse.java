package db.ninja.post.dto;


import db.ninja.common.vo.Location;
import db.ninja.post.vo.Money;
import java.time.LocalDateTime;
import java.util.List;


public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        Long categoryId,
        String categoryName,
        Money price,
        String status,
        Location tradeLocation,
        List<String> productImageUrls,
        int viewCount,
        int favoriteCount,
        boolean isFavorited,
        LocalDateTime createdAt) {

}
