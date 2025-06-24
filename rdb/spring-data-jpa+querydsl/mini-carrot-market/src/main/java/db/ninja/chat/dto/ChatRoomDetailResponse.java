package db.ninja.chat.dto;


import db.ninja.common.vo.Location;
import java.time.LocalDateTime;
import java.util.List;


public record ChatRoomDetailResponse(
        Long chatRoomId,
        Long postId,
        Long userId,
        Long partnerId,
        String postThumbnailUrl,
        Location postLocation,
        String postStatus,
        String chatRoomTitle,
        String userNickname,
        String partnerNickname,
        LocalDateTime userLastReadMessageAt,
        List<ChatMessageResponse> chatMessages,
        boolean isReadOnly) {

}
