package db.ninja.post.dto;


import db.ninja.common.vo.Location;
import db.ninja.post.vo.PostStatus;
import lombok.Builder;


@Builder
public record PostSearchCondition(
        Long categoryId,
        String title,
        PostStatus status,
        int minPrice,
        int maxPrice,
        Location userLocation) {

}
