package db.ninja.post.dto;


import db.ninja.post.entity.Post;


// 게시글 목록 또는 상세 조회 시 사용하는 프로젝션용 DTO
// thumbnailUrl 필드는 게시글 목록 조회 시에만 사용된다
// 연관 엔티티 페치 조인 여부는 리포지토리 주석에 명시되어 있다
public record PostWithFavoriteCountDto(
        Post post, String thumbnailUrl, int favoriteCount) {

}
