package db.ninja.chat.repository;


import db.ninja.chat.dto.ChatRoomDetailResponse;
import db.ninja.chat.dto.ChatRoomSummaryResponse;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


public interface ChatRoomCustomRepository {

    Optional<ChatRoomDetailResponse> findChatRoomDetailByUserIdAndPostId(Long userId, Long postId);
    Optional<ChatRoomDetailResponse> findChatRoomDetailByUserIdAndRoomId(Long userId, Long roomId);
    Slice<ChatRoomSummaryResponse> findChatRoomSummaryAllByUserId(Long userId, Pageable pageable);

}
