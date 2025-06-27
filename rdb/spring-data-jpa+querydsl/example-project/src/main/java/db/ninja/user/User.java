package db.ninja.user;


import db.ninja.common.entity.BaseTimeEntity;
import db.ninja.common.vo.Location;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.*;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    // 제약조건 상수
    private static final int LOCATION_HOLD_MAXIMUM = 3;
    private static final long NICKNAME_CHANGE_INTERVAL = Duration.ofDays(7).toDays();
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // 필드
    // 엔티티 기본키 지정
    @Id
    // 기본키 생성 전략 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 컬럼 속성 지정
    @Column(nullable = false, length = 100, unique = true)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    private LocalDateTime lastNicknameChangedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // 값 타입 컬렉션 매핑 시 사용
    @ElementCollection
    // 별도 테이블에 위치 정보 저장
    @CollectionTable(name = "user_locations", joinColumns = @JoinColumn(name = "user_id"))
    private List<Location> locations = new ArrayList<>();

    // 생성자
    @Builder
    public User(String username, String password, String nickname, Location location) {
        if (isValidEmail(username)) {
            throw new IllegalArgumentException("이메일 형식의 아이디만 사용할 수 있습니다");
        }

        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.locations.add(location);
        this.status = UserStatus.ACTIVE;
        this.lastNicknameChangedAt = null;
    }

    // 도메인 규칙 메서드 (비즈니스 규칙 메서드)
    public static boolean isValidEmail(String email) {
        return EMAIL_REGEX.matcher(email).matches();
    }

    public boolean canChangeNickname(LocalDateTime now) {
        return lastNicknameChangedAt == null || now.isAfter(lastNicknameChangedAt.plusDays(NICKNAME_CHANGE_INTERVAL));
    }

    // 도메인 행위 메서드 (상태 변경 메서드)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String newNickname) {
        LocalDateTime now = LocalDateTime.now();

        if (!canChangeNickname(now)) {
            throw new IllegalArgumentException(
                    lastNicknameChangedAt.plusDays(NICKNAME_CHANGE_INTERVAL).toString().concat("이후 닉네임을 변경할 수 있습니다"));
        }

        this.nickname = newNickname;
        this.lastNicknameChangedAt = now;
    }

    public void addLocation(Location location) {
        if (locations.size() > LOCATION_HOLD_MAXIMUM) {
            throw new IllegalArgumentException("최대 3개의 위치만 등록할 수 있습니다");
        }

        if (locations.contains(location)) {
            throw new IllegalArgumentException("이미 등록된 위치입니다");
        }

        locations.add(location);
    }

    public void removeLocation(Location location) {
        if (!locations.contains(location)) {
            throw new IllegalArgumentException("등록되지 않은 위치입니다");
        }

        if (locations.size() == 1) {
            throw new IllegalArgumentException("최소 하나의 위치는 등록되어 있어야 합니다");
        }

        locations.remove(location);
    }

    public void delete() {
        this.status = UserStatus.DELETE_SCHEDULED;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User user)) return false;

        return id != null && id.equals(user.id);
    }

}