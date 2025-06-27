package db.ninja.review.dto;


import java.time.LocalDateTime;


public record ReviewResponse(
        Long reviewId,
        Long reviewerId,
        Long revieweeId,
        Long postId,
        String postTitle,
        String postThumbnailUrl,
        String reviewContent,
        int rating,
        LocalDateTime createdAt) {

}
