package db.ninja;


import java.util.Optional;


public interface UserQuerydslRepository {

    Optional<User> findByNameQuerydsl(String name);

}
