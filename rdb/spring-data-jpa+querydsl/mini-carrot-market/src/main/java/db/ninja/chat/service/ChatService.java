package db.ninja.chat.service;


import db.ninja.chat.entity.ChatMessage;
import db.ninja.chat.entity.ChatParticipant;
import db.ninja.chat.entity.ChatRoom;
import db.ninja.chat.repository.ChatMessageRepository;
import db.ninja.chat.repository.ChatParticipantRepository;
import db.ninja.chat.repository.ChatRoomRepository;
import db.ninja.post.entity.Post;
import db.ninja.post.service.PostQueryService;
import db.ninja.user.User;
import db.ninja.user.UserQueryService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final UserQueryService userQueryService;
    private final PostQueryService postQueryService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public Long createChatRoom(Long postId, Long buyerId, Long sellerId) {
        Optional<ChatRoom> existingChatRoomOptional = chatRoomRepository.findByParticipantsIdFetchJoin(buyerId, sellerId);

        // 채팅방이 이미 존재하는 경우 채팅방을 다시 만들지 않으며 채팅방을 나간 참여자가 있다면 재입장시킨다
        if (existingChatRoomOptional.isPresent()) {
            ChatRoom existingChatRoom = existingChatRoomOptional.get();
            List<ChatParticipant> participants = existingChatRoom.getParticipants();
            participants.forEach(ChatParticipant::rejoinChatRoomIfLeaved);
            return existingChatRoom.getId();
        }

        Post post = postQueryService.getPostById(postId);
        List<User> users = userQueryService.getUsersByIds(List.of(buyerId, sellerId));
        ChatRoom chatRoom = new ChatRoom(post, users.get(0), users.get(1));

        ChatRoom save = chatRoomRepository.save(chatRoom);
        return save.getId();
    }

    public void leaveChatRoom(Long chatRoomId, Long userId) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(chatRoomId, userId).orElseThrow();
        chatParticipant.leaveChatRoom();
    }

    public void sendMessage(Long chatRoomId, Long senderId, String content) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndUserIdFetchJoin(chatRoomId, senderId).orElseThrow();
        ChatRoom chatRoom = chatParticipant.getChatRoom();
        User sender = chatParticipant.getUser();

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .build();

        chatMessageRepository.save(chatMessage);
    }

}
