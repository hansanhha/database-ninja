package db.ninja.chat.dto;


import java.time.LocalDateTime;


public record ChatMessageResponse(
        Long chatMessageId,
        Long chatRoomId,
        Long senderId,
        String senderNickname,
        String messageContent,
        LocalDateTime sentAt) {

}
