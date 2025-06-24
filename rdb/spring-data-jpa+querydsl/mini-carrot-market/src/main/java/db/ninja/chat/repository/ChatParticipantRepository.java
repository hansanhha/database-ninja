package db.ninja.chat.repository;


import db.ninja.chat.entity.ChatParticipant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface ChatParticipantRepository extends CrudRepository<ChatParticipant, Long> {

    @Query(
            """
            SELECT cp
            FROM ChatParticipant cp
            WHERE cp.chatRoom.id = :chatRoomId AND cp.user.id = :userId
            """
    )
    Optional<ChatParticipant> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    @Query(
            """
            SELECT cp
            FROM ChatParticipant cp
            JOIN FETCH cp.user
            JOIN FETCH cp.chatRoom
            WHERE cp.chatRoom.id = :chatRoomId AND cp.user.id = :userId
            """
    )
    Optional<ChatParticipant> findByChatRoomIdAndUserIdFetchJoin(Long chatRoomId, Long userId);

}
