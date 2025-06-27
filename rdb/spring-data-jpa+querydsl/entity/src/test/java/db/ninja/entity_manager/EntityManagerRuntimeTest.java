package db.ninja.entity_manager;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@DisplayName("엔티티 매니저 런타임(프록시) 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class EntityManagerRuntimeTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 스프링은_프록시_엔티티매니저를_사용한다() {

        System.out.println(entityManager.getClass());

    }

}
