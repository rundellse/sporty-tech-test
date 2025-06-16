package com.rundellse.sportytechtest.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {

    private UUID commentId;

    private UUID ticketId;

    private String authorId;

    private String content;

    private Visibility visibility;

    private LocalDateTime createdAt;

    public Comment(UUID commentId, UUID ticketId, String authorId, String content, Visibility visibility, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.content = content;
        this.visibility = visibility;
        this.createdAt = createdAt;
    }

    public UUID getCommentId() {
        return commentId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
