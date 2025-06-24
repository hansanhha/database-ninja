package db.ninja.post.repository;


import db.ninja.post.dto.PostSearchCondition;
import db.ninja.post.dto.PostWithFavoriteCountDto;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


public interface PostCustomRepository {

    /**
     * 게시글 상세 정보 조회<br>
     * 페치 조인한 게시글 작성자, 카테고리, 이미지와 집계된 관심수를 포함한 DTO를 반환한다
     */
    Optional<PostWithFavoriteCountDto> findPostDetailByIdFetchJoin(Long postId);

    /**
     * 게시글 검색 조건에 맞는 게시글 목록 조회<br>
     * 페치 조인을 하지 않고 게시글 목록과 각 게시글의 첫 번째 상품 이미지 및 관심 수를 포함한 DTO 슬라이스를 반환한다
     */
    Slice<PostWithFavoriteCountDto> searchPostSummaryByCondition(PostSearchCondition condition, Pageable pageable);

    /**
     * 특정 사용자가 좋아요한 게시글 목록 조회<br>
     * 페치 조인을 하지 않고 게시글 목록과 각 게시글의 첫 번째 상품 이미지 및 관심 수를 포함한 DTO 슬라이스를 반환한다
     */
    Slice<PostWithFavoriteCountDto> findFavoritePostsByUserId(Long userId, Pageable pageable);

}
