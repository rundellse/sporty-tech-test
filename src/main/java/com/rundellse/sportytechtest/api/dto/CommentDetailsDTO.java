package com.rundellse.sportytechtest.api.dto;

import com.rundellse.sportytechtest.model.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDetailsDTO(
        UUID commentId,
        UUID ticketId,
        String authorId,
        String content,
        String visibility,
        LocalDateTime createdAt
) {
    public CommentDetailsDTO(Comment comment) {
        this(
                comment.getCommentId(),
                comment.getTicketId(),
                comment.getAuthorId(),
                comment.getContent(),
                comment.getVisibility().toString(),
                comment.getCreatedAt()
        );
    }
}
