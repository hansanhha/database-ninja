package db.ninja.chat.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import db.ninja.chat.dto.ChatMessageResponse;
import db.ninja.chat.entity.QChatMessage;
import db.ninja.chat.entity.QChatParticipant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository {

    private final ChatMessageQueryHelper messageQueryHelper;
    private final JPAQueryFactory query;
    private final QChatMessage message = QChatMessage.chatMessage;
    private final QChatParticipant participant = QChatParticipant.chatParticipant;

    @Override
    public Slice<ChatMessageResponse> searchMessagesByRoomIdAndKeyword(Long userId, Long roomId, String keyword, Pageable pageable) {
        List<ChatMessageResponse> result = query
                .select(Projections.constructor(ChatMessageResponse.class,
                        message.id,
                        message.chatRoom.id,
                        message.sender.id,
                        message.sender.nickname,
                        message.content,
                        message.createdAt
                ))
                .from(message)
                .where(message.content.likeIgnoreCase(keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.removeLast();
        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Override
    public Slice<ChatMessageResponse> findChatMessagesByRoomId(Long userId, Long roomId, LocalDateTime findBeforeDate) {
        LocalDateTime rejoinedAt = JPAExpressions
                .select(participant.rejoinedAt)
                .from(participant)
                .where(participant.user.id.eq(userId), participant.chatRoom.id.eq(roomId))
                .fetchOne();

        List<ChatMessageResponse> chatMessageResponses = messageQueryHelper.fetchMessagesByRoomId(roomId, rejoinedAt, findBeforeDate, 21);
        boolean hasNext = chatMessageResponses.size() > 20;
        if (hasNext) chatMessageResponses.removeLast();
        return new SliceImpl<>(chatMessageResponses, Pageable.unpaged(), hasNext);
    }

}
