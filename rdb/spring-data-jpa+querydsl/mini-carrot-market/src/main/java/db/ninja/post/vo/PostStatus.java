package db.ninja.post.vo;


import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum PostStatus {

    ON_SALE(Set.of("HIDDEN", "RESERVED"), "판매 중"),
    RESERVED(Set.of("SOLD", "ON_SALE"), "예약 중"),
    SOLD(Set.of("HIDDEN"), "판매 완료"),
    HIDDEN(Set.of("SOLD", "ON_SALE"), "숨겨짐");

    private final Set<String> allowedTransitions;

    @Getter
    private final String displayName;

    public boolean canTransition(PostStatus newStatus) {
        return allowedTransitions.contains(newStatus.name());
    }

}
