package db.ninja.chat.entity;


import db.ninja.user.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import db.ninja.post.entity.Post;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private boolean isReadOnly = false;

    public ChatRoom(Post post, User user1, User user2) {
        this.post = post;
        ChatParticipant participant1 = new ChatParticipant(this, user1);
        ChatParticipant participant2 = new ChatParticipant(this, user2);
        participants.addAll(List.of(participant1, participant2));
    }

    public void deactivate() {
        isReadOnly = true;
    }

}
