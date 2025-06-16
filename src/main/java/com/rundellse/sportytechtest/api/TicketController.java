package com.rundellse.sportytechtest.api;

import com.rundellse.sportytechtest.api.dto.*;
import com.rundellse.sportytechtest.model.Comment;
import com.rundellse.sportytechtest.model.Ticket;
import com.rundellse.sportytechtest.model.TicketStatus;
import com.rundellse.sportytechtest.model.Visibility;
import com.rundellse.sportytechtest.persistence.TicketRepository;
import com.rundellse.sportytechtest.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
public class TicketController {

    private final TicketRepository ticketRepository;

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketRepository ticketRepository, TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
    }


    @PostMapping("/tickets")
    public ResponseEntity<TicketDetailsDTO> createTicket(@RequestBody CreateTicketDTO createTicketDTO) {
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                createTicketDTO.subject(),
                createTicketDTO.description(),
                TicketStatus.OPEN,
                createTicketDTO.userId(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ticket = ticketRepository.saveTicket(ticket);
        return new ResponseEntity<>(new TicketDetailsDTO(ticket), HttpStatus.CREATED);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDetailsDTO>> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String assigneeId
    ) {
        TicketStatus ticketStatus = status == null ? null : TicketStatus.valueOf(status);

        List<TicketDetailsDTO> allTickets = new ArrayList<>();
        for (Ticket ticket : ticketRepository.getTicketsFiltered(ticketStatus, userId, assigneeId)) {
            List<Comment> comments = ticketService.getFilteredCommentsForTicket(ticket);

            TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO(ticket, comments);
            allTickets.add(ticketDetailsDTO);
        }

        return ResponseEntity.ok(allTickets);
    }

    @PatchMapping("/tickets/{ticketId}/status")
    public ResponseEntity<TicketDetailsDTO> updateTicketStatus(
            @PathVariable UUID ticketId,
            @RequestBody UpdateStatusDTO updateStatusDTO
    ) {
        TicketStatus newStatus = TicketStatus.valueOf(updateStatusDTO.status());
        Ticket ticket = ticketRepository.getTicket(ticketId);
        if (!ticketService.validateTicketStatusChange(ticket.getStatus(), newStatus)) {
            //Log details
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        return ResponseEntity.ok(new TicketDetailsDTO(ticket));
    }


    @PostMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<CommentDetailsDTO> addCommentToTicket(
            @PathVariable UUID ticketId,
            @RequestBody AddCommentDTO addCommentDTO
    ) {
        Ticket ticket = ticketRepository.getTicket(ticketId);
        if (ticket == null) {
            // Log
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Visibility visibility = Visibility.valueOf(addCommentDTO.visibility());
        if (addCommentDTO.authorId().startsWith("user-") && visibility != Visibility.PUBLIC) {
            // Log
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Comment comment = new Comment(
                UUID.randomUUID(),
                ticketId,
                addCommentDTO.authorId(),
                addCommentDTO.content(),
                visibility,
                LocalDateTime.now()
        );
        comment = ticketService.saveComment(comment);

        return new ResponseEntity<>(new CommentDetailsDTO(comment), HttpStatus.CREATED);
    }
}
