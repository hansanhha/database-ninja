package db.ninja.n1_problem;


import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@DisplayName("N+1 문제 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class N1ProblemTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostEntityGraphRepository postEntityGraphRepository;

    @BeforeEach
    void setUp () {
        for (int i = 1; i <= 100; i++) {
            User user = new User("user " + i);
            em.persist(user);

            Post post = new Post("post " + i, user);
            em.persist(post);

            PostEntityGraph postEntityGraph = new PostEntityGraph("post " + i, user);
            em.persist(postEntityGraph);
        }

        em.flush();
        em.clear();
    }

    @AfterEach
    void tearDown() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();
    }

    @Test
    void 게시글_100개_조회_1개의_쿼리에_연관엔티티_조회쿼리가_100개_발생한다() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        List<Post> posts = (List<Post>) postRepository.findAll();

        for (Post post : posts) {
            System.out.println(post.getUser().getUsername());
        }

        long postQueryCount = stats.getQueryExecutionCount();
        long userFetchCount = stats.getEntityFetchCount();

        assertThat(postQueryCount + userFetchCount).isEqualTo(101);
    }

    @Test
    void 네임드_엔티티_그래프를_사용하여_해결할수있다() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        List<PostEntityGraph> posts = postEntityGraphRepository.findAll();

        for (PostEntityGraph post : posts) {
            System.out.println(post.getUser().getUsername());
        }

        long postQueryCount = stats.getQueryExecutionCount();
        long userFetchCount = stats.getEntityFetchCount();

        assertThat(postQueryCount + userFetchCount).isEqualTo(1);
    }

    @Test
    void 페치조인을_사용하여_해결할수있다() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        List<Post> posts = postRepository.findAllFetchJoin();

        for (Post post : posts) {
            System.out.println(post.getUser().getUsername());
        }

        long postQueryCount = stats.getQueryExecutionCount();
        long userFetchCount = stats.getEntityFetchCount();

        assertThat(postQueryCount + userFetchCount).isEqualTo(1);
    }

    @Test
    void DTO프로젝션을_사용하여_해결할수있다() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        List<PostDto> posts = postRepository.findAllProjection();

        for (PostDto post : posts) {
            System.out.println(post.username());
        }

        long postQueryCount = stats.getQueryExecutionCount();
        long userFetchCount = stats.getEntityFetchCount();

        assertThat(postQueryCount + userFetchCount).isEqualTo(1);
    }

}
