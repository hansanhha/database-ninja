package db.ninja.user;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
// 읽기 전용 트랜잭션 적용
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public List<User> getUsersByIds(List<Long> ids) {
        return (List<User>) userRepository.findAllById(ids);
    }

    public UserProfile getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return new UserProfile(user.getUsername(), user.getNickname(), user.getLocations().getFirst(), user.getCreatedAt());
    }


}
