package db.ninja.entity_manager;


import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class UserQuerydslRepositoryImpl implements UserQuerydslRepository {

    private final QUser user = QUser.user;
    private final JPAQueryFactory query;

    @Override
    public Optional<User> findByNameQuerydsl(String name) {
        User found = query.selectFrom(user)
                .where(user.name.eq(name))
                .fetchOne();

        return Optional.ofNullable(found);
    }

}
