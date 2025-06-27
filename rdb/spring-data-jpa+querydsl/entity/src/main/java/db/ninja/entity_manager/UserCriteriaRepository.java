package db.ninja.entity_manager;


import java.util.Optional;


public interface UserCriteriaRepository {

    Optional<User> findByNameCriteria(String name);

}
