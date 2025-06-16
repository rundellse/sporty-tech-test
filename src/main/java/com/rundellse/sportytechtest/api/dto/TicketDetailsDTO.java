package com.rundellse.sportytechtest.api.dto;

import com.rundellse.sportytechtest.model.Comment;
import com.rundellse.sportytechtest.model.Ticket;
import com.rundellse.sportytechtest.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketDetailsDTO(
        UUID ticketId,
        String subject,
        String description,
        TicketStatus status,
        String userId,
        String assigneeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentDetailsDTO> comments
) {
    public TicketDetailsDTO(Ticket ticket) {
        this(
                ticket.getTicketId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getUserId(),
                ticket.getAssigneeId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                null
        );
    }

    public TicketDetailsDTO(Ticket ticket, List<Comment> comments) {
        this(
                ticket.getTicketId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getUserId(),
                ticket.getAssigneeId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                comments.stream().map(CommentDetailsDTO::new).toList()
        );
    }
}
