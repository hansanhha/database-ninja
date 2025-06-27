package db.ninja.chat.service;


import db.ninja.chat.dto.ChatMessageResponse;
import db.ninja.chat.dto.ChatRoomDetailResponse;
import db.ninja.chat.dto.ChatRoomSummaryResponse;
import db.ninja.chat.repository.ChatMessageRepository;
import db.ninja.chat.repository.ChatParticipantRepository;
import db.ninja.chat.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomDetailResponse getChatRoomByPostId(Long userId, Long postId) {

        return chatRoomRepository.findChatRoomDetailByUserIdAndPostId(userId, postId).orElseThrow();
    }

    public ChatRoomDetailResponse getChatRoomByRoomId(Long userId, Long roomId) {
        return chatRoomRepository.findChatRoomDetailByUserIdAndRoomId(userId, roomId).orElseThrow();
    }

    public Slice<ChatRoomSummaryResponse> getChatRooms(Long userId, Pageable pageable) {
        return chatRoomRepository.findChatRoomSummaryAllByUserId(userId, pageable);
    }

    public Slice<ChatMessageResponse> getChatMessages(Long userId, Long roomId, LocalDateTime findBeforeDate) {
        return chatMessageRepository.findChatMessagesByRoomId(userId, roomId, findBeforeDate);
    }

    public Slice<ChatMessageResponse> searchMessages(Long userId, Long roomId, String keyword, Pageable pageable) {
        return chatMessageRepository.searchMessagesByRoomIdAndKeyword(userId, roomId, keyword, pageable);
    }

}
