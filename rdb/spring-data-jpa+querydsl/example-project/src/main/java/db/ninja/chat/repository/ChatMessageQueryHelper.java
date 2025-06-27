package db.ninja.chat.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import db.ninja.chat.dto.ChatMessageResponse;
import db.ninja.chat.entity.QChatMessage;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ChatMessageQueryHelper {

    private final JPAQueryFactory query;
    private final QChatMessage message = QChatMessage.chatMessage;

    public List<ChatMessageResponse> fetchMessagesByRoomId(Long roomId, @Nullable LocalDateTime fetchAfterDate, @Nullable LocalDateTime fetchBeforeDate, int limit) {
        return query
                .select(
                        Projections.constructor(ChatMessageResponse.class,
                                message.id,
                                message.chatRoom.id,
                                message.sender.id,
                                message.sender.nickname,
                                message.content,
                                message.createdAt)
                )
                .from(message)
                .where(
                        message.chatRoom.id.eq(roomId),
                        fetchAfterDate != null ? message.createdAt.after(fetchAfterDate) : null,
                        fetchBeforeDate != null ? message.createdAt.before(fetchBeforeDate) : null
                )
                .orderBy(message.createdAt.desc())
                .limit(limit)
                .fetch();
    }

}
