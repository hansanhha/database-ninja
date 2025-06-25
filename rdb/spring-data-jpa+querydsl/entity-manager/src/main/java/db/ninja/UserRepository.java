package db.ninja;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository("entityManagerUserRepository")
public interface UserRepository extends JpaRepository<User, Long> {

}
