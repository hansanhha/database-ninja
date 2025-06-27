package db.ninja.entity_manager;


import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class UserCriteriaRepositoryImpl implements UserCriteriaRepository {

    private final EntityManager em;

    @Override
     public Optional<User> findByNameCriteria(String name) {
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<User> query = cb.createQuery(User.class);
         Root<User> userRoot = query.from(User.class);
         query.select(userRoot).where(cb.equal(userRoot.get("name"), name));
         User found = em.createQuery(query).getSingleResult();
         return Optional.ofNullable(found);
     }
}
