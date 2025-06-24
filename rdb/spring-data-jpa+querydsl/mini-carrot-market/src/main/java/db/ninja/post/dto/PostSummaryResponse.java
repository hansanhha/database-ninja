package db.ninja.post.dto;


import db.ninja.common.vo.Location;
import db.ninja.post.vo.Money;


public record PostSummaryResponse(
        Long postId,
        String thumbnailImageUrl,
        String title,
        Location tradeLocation,
        String status,
        Money price,
        int viewCount,
        int favoriteCount) {

}
