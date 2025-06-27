package db.ninja.entity_manager;


import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@DisplayName("영속성 상태 전이 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PersistenceStateTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @Transactional
    void DETACHED_상태의_엔티티를_merge하면_모든값이_덮어쓰인다() {
        Statistics stats = getStatistics();
        stats.setStatisticsEnabled(true);

        // 엔티티 영속화 (MANAGED)
        User user = em.persist(new User("test user", 10));
        assertThat(em.getEntityManager().contains(user)).isTrue();
        em.flush();

        // 영속성 컨텍스트에서 분리 (DETACHED)
        em.detach(user);
        assertThat(em.getEntityManager().contains(user)).isFalse();

        // DETACHED 상태의 엔티티 필드 변경 후 merge
        // 기존 엔티티의 필드를 DETACHED 엔티티의 필드로 모두 덮어쓴다
        user.setName("update name");
        user.setAge(100);
        User merged = em.merge(user);
        em.flush();

        // DETACHED는 여전히 영속성 컨텍스트에 포함되지 않는다
        assertThat(em.getEntityManager().contains(merged)).isTrue();
        assertThat(em.getEntityManager().contains(user)).isFalse();

        // merge된 엔티티의 필드 값으로 모두 덮어쓴다
        assertThat(merged.getName()).isEqualTo(user.getName());
        assertThat(merged.getAge()).isEqualTo(user.getAge());

        assertThat(stats.getEntityInsertCount()).isEqualTo(1);
        assertThat(stats.getEntityLoadCount()).isEqualTo(1);
        assertThat(stats.getEntityUpdateCount()).isEqualTo(1);
    }

    private Statistics getStatistics() {
        SessionFactory sessionFactory = em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
        return sessionFactory.getStatistics();
    }

}
