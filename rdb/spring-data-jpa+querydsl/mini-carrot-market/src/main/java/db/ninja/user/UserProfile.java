package db.ninja.user;


import db.ninja.common.vo.Location;
import java.time.LocalDateTime;


public record UserProfile(
        String username,
        String nickname,
        Location location,
        LocalDateTime createdAt) {

}
