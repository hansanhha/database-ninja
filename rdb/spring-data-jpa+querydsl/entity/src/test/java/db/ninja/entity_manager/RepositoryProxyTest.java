package db.ninja.entity_manager;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@DisplayName("리포지토리 프록시 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class RepositoryProxyTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 리포지토리는_프록시_객체가_주입된다() {
        assertThat(AopUtils.isAopProxy(userRepository)).isTrue();
    }

    @Test
    void 리포지토리_프록시의_대상은_SimpleJpaReository이다() {
        assertThat(AopProxyUtils.ultimateTargetClass(userRepository)).isEqualTo(SimpleJpaRepository.class);
    }

    @Test
    void querydsl메서드는_ImplementationMethodExecutionInterceptor가_처리한다() {
        userRepository.save(new User("test user", 10));
        userRepository.findByNameQuerydsl("test user");
    }

    

}
