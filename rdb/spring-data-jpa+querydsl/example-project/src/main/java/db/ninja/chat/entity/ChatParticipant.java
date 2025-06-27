package db.ninja.chat.entity;


import db.ninja.common.entity.BaseTimeEntity;
import db.ninja.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(
        uniqueConstraints = {
                // 사용자 간 채팅방 중복 참여 방지 유니크 제약조건
                @UniqueConstraint(name = "uc_chat_participant_chat_room_user", columnNames = {"chat_room_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 채팅방 메시지 읽음 여부를 표시하기 위해 마지막으로 읽은 메시지 시각 기록
    private LocalDateTime lastReadMessageAt;

    // 채팅방 재입장 시 기존 대화 메시지 내용 이후부터 읽을 수 있도록 재입장 시각 기록
    private LocalDateTime rejoinedAt;

    private boolean isLeaved = false;

    public ChatParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public void readMessage() {
        if (isLeaved) throw new IllegalArgumentException("채팅방을 나간 사용자는 메시지를 읽을 수 없습니다");

        this.lastReadMessageAt = LocalDateTime.now();
    }

    public void leaveChatRoom() {
        this.isLeaved = true;
    }

    public void rejoinChatRoomIfLeaved() {
        if (isLeaved) {
            this.isLeaved = false;
            this.rejoinedAt = LocalDateTime.now();
        }
    }

}
