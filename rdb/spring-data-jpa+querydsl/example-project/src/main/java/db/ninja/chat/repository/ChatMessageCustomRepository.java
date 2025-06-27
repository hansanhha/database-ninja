package db.ninja.chat.repository;


import db.ninja.chat.dto.ChatMessageResponse;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


public interface ChatMessageCustomRepository {

    Slice<ChatMessageResponse> findChatMessagesByRoomId(Long userId, Long roomId, LocalDateTime findBeforeDate);
    Slice<ChatMessageResponse> searchMessagesByRoomIdAndKeyword(Long userId, Long roomId, String keyword, Pageable pageable);

}
