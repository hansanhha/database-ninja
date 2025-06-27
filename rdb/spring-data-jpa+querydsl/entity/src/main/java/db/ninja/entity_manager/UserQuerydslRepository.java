package db.ninja.entity_manager;


import java.util.Optional;


public interface UserQuerydslRepository {

    Optional<User> findByNameQuerydsl(String name);

}
