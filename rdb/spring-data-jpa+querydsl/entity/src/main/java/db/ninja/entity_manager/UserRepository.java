package db.ninja.entity_manager;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository("entityManagerUserRepository")
public interface UserRepository extends JpaRepository<User, Long>, UserQuerydslRepository, UserCriteriaRepository {

    Optional<User> findByName(String name);

    @Query("Select u from User u where u.name = :name")
    Optional<User> findByNameJPQL(String name);
}
