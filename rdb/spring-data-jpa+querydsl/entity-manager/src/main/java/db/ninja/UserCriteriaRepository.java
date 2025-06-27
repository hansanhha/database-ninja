package db.ninja;


import java.util.Optional;


public interface UserCriteriaRepository {

    Optional<User> findByNameCriteria(String name);

}
