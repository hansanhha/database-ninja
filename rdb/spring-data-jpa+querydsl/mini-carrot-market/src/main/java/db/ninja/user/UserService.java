package db.ninja.user;


import db.ninja.common.vo.Location;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signUp(String username, String password, String nickname, Location location) {
        validateDuplicateUsername(username);
        validateDuplicateNickname(nickname);

        User user = new User(username, password, nickname, location);
        userRepository.save(user);
    }

    public AuthenticationToken signIn(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다"));

        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다");
        }

        return new AuthenticationToken(user.getUsername());
    }

    // AuthenticationToken은 사용자 인증 정보를 담고 있는 객체로 가정한다
    public void updatePassword(AuthenticationToken token, String newPassword) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.changePassword(newPassword);
    }

    public void deleteUser(AuthenticationToken token) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.delete();
    }

    public void updateUserNickname(AuthenticationToken token, String newNickname) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        validateDuplicateNickname(newNickname);
        user.changeNickname(newNickname);
    }

    public void addLocation(AuthenticationToken token, Location location) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.addLocation(location);
    }

    public void removeLocation(AuthenticationToken token, Location location) {
        User user = userRepository.findByUsername(token.username()).orElseThrow();
        user.removeLocation(location);
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
        }
    }

}
