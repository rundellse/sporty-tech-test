package com.rundellse.sportytechtest.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {

    private UUID ticketId;

    private String subject;

    private String description;

    private TicketStatus status;

    private String userId;

    private String assigneeId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Ticket(UUID ticketId, String subject, String description, TicketStatus status, String userId, String assigneeId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ticketId = ticketId;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.assigneeId = assigneeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
