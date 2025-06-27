package db.ninja.chat.repository;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import db.ninja.chat.dto.ChatMessageResponse;
import db.ninja.chat.dto.ChatRoomDetailResponse;
import db.ninja.chat.dto.ChatRoomSummaryResponse;
import db.ninja.chat.entity.QChatMessage;
import db.ninja.chat.entity.QChatParticipant;
import db.ninja.chat.entity.QChatRoom;
import db.ninja.post.entity.QPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ChatRoomCustomRepositoryImpl implements ChatRoomCustomRepository {

    private final ChatMessageQueryHelper messageQueryHelper;
    private final JPAQueryFactory query;
    private final QChatRoom room = QChatRoom.chatRoom;
    private final QChatParticipant participant = QChatParticipant.chatParticipant;
    private final QChatMessage message = QChatMessage.chatMessage;
    private final QPost post = QPost.post;

    @Override
    public Optional<ChatRoomDetailResponse> findChatRoomDetailByUserIdAndPostId(Long postId, Long userId) {
        return getChatRoomDetailByPredicates(room.post.id.eq(postId), participant.user.id.eq(userId));
    }

    @Override
    public Optional<ChatRoomDetailResponse> findChatRoomDetailByUserIdAndRoomId(Long roomId, Long userId) {
        return getChatRoomDetailByPredicates(room.id.eq(roomId), participant.user.id.eq(userId));
    }

    @Override
    public Slice<ChatRoomSummaryResponse> findChatRoomSummaryAllByUserId(Long userId, Pageable pageable) {
        QChatMessage latestMessage = new QChatMessage("latestMessage");

        SubQueryExpression<Long> latestMessageId = JPAExpressions
                .select(message.id)
                .from(message)
                .where(message.chatRoom.id.eq(room.id))
                .orderBy(message.createdAt.desc())
                .limit(1);

        List<ChatRoomSummaryResponse> result = query
                .select(Projections.constructor(ChatRoomSummaryResponse.class,
                        room.id,
                        room.post.productImages.get(0).url,
                        room.post.tradeLocation,
                        room.post.status,
                        room.post.title,
                        JPAExpressions.
                                select(message.sender.nickname)
                                .from(message)
                                .where(message.id.eq(latestMessageId)),
                        JPAExpressions.
                                select(message.content)
                                .from(message)
                                .where(message.id.eq(latestMessageId)))
                )
                .from(participant)
                .join(participant.chatRoom, room)
                .join(room.post, post)
                .where(participant.user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // hasNext 여부를 판단하기 위해 +1
                .fetch();

        boolean hasNext = result.size() > pageable.getPageSize();
        if (hasNext) result.removeLast();
        return new SliceImpl<>(result, pageable, hasNext);
    }

    private Optional<ChatRoomDetailResponse> getChatRoomDetailByPredicates(Predicate... predicates) {
        QChatParticipant userCP = new QChatParticipant("user");
        QChatParticipant partnerCP = new QChatParticipant("partner");

        Tuple result = findChatRoomDetail(predicates);
        if (result == null) return Optional.empty();

        LocalDateTime userRejoinedAt = result.get(userCP.rejoinedAt);
        List<ChatMessageResponse> chatMessageDtos = messageQueryHelper.fetchMessagesByRoomId(result.get(room.id), userRejoinedAt, LocalDateTime.now(), 20);

        return Optional.of(new ChatRoomDetailResponse(
                result.get(room.id),
                result.get(post.id),
                result.get(userCP.user.id),
                result.get(partnerCP.user.id),
                result.get(post.productImages.get(0).url),
                result.get(post.tradeLocation),
                result.get(post.status).getDisplayName(),
                result.get(post.title),
                result.get(userCP.user.nickname),
                result.get(partnerCP.user.nickname),
                result.get(userCP.lastReadMessageAt),
                chatMessageDtos,
                Boolean.TRUE.equals(result.get(room.isReadOnly))
        ));
    }

    private Tuple findChatRoomDetail(Predicate... predicates) {
        QChatParticipant userCP = new QChatParticipant("user");
        QChatParticipant partnerCP = new QChatParticipant("partner");

        return query
                .select(
                        room.id, post.id, userCP.user.id, partnerCP.user.id,
                        post.title, post.productImages.get(0).url,
                        userCP.user.nickname, partnerCP.user.nickname,
                        userCP.lastReadMessageAt, userCP.rejoinedAt, room.isReadOnly
                )
                .from(room)
                .join(room.post, post)
                .join(room.participants, userCP)
                .join(room.participants, partnerCP)
                .where(predicates)
                .fetchOne();
    }

}
