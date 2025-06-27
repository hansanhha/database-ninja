package db.ninja.chat.repository;


import db.ninja.chat.entity.ChatRoom;
import db.ninja.chat.dto.ChatRoomDetailResponse;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface ChatRoomRepository extends CrudRepository<ChatRoom, Long>, ChatRoomCustomRepository {


    @Query(
            """
            SELECT cr
            FROM ChatRoom cr
            JOIN ChatParticipant cp ON cr.id = cp.chatRoom.id
            WHERE cp.user = :buyerId
            """
    )
    Optional<ChatRoomDetailResponse> findChatRoomDetailByParticipantId(Long participantId);

    /**
     * 특정 사용자들이 참여한 채팅방을 조회한다
     * 페치조인: 채팅방 참여자 (판매자, 구매자)
     */
    @Query(
            """
            SELECT cr
            FROM ChatRoom cr
            JOIN FETCH cr.participants cp
            WHERE cp.user.id = :participantId1 AND cp.user.id = :participantId2
            """
    )
    Optional<ChatRoom> findByParticipantsIdFetchJoin(
            @Param("participantId1") Long participantId1, @Param("participantId2") Long participantId2);

}
