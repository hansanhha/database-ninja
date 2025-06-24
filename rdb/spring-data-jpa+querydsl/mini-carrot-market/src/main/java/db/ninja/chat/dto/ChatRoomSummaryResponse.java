package db.ninja.chat.dto;


import db.ninja.common.vo.Location;
import db.ninja.post.vo.PostStatus;


public record ChatRoomSummaryResponse(
        Long chatRoomId,
        String postThumbnailUrl,
        Location postLocation,
        String postStatus,
        String chatRoomTitle,
        String lastSenderNickname,
        String lastMessageContent) {

    public ChatRoomSummaryResponse {
        postStatus = PostStatus.valueOf(postStatus).getDisplayName();
    }

}
