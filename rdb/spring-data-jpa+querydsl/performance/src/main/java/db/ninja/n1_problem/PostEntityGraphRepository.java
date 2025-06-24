package db.ninja.n1_problem;


import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository("n1ProblemPostEntityGraphRepository")
public interface PostEntityGraphRepository extends CrudRepository<PostEntityGraph, Long> {

    // 네임드 엔티티 그래프에 지정된 이름 명시
    @EntityGraph(value = "post.writer")
    List<PostEntityGraph> findAll();

    // 조인할 경로 직접 명시
//    @EntityGraph(attributePaths = "user")
//    List<PostEntityGraph> findAll();
}
