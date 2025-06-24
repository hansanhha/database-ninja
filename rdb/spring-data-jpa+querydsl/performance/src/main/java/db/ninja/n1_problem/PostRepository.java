package db.ninja.n1_problem;


import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository("n1ProblemPostRepository")
public interface PostRepository extends CrudRepository<Post, Long> {

    // Post 엔티티 조회 시 연관 엔티티 User도 함께 메모리에 로딩한다
    @Query("SELECT p FROM Post p JOIN FETCH p.user u")
    List<Post> findAllFetchJoin();

    // DTO 프로젝션을 사용하여 필요한 정보만 메모리에 로딩한다
    @Query("SELECT new db.ninja.n1_problem.PostDto(p.title, u.username) " +
            "FROM Post p " +
            "JOIN p.user u")
    List<PostDto> findAllProjection();
}
