package com.rundellse.sportytechtest.persistence;

import com.rundellse.sportytechtest.model.Ticket;
import com.rundellse.sportytechtest.model.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TicketRepository {

    private final ConcurrentHashMap<UUID, Ticket> tickets;

    @Autowired
    public TicketRepository() {
        this.tickets = new ConcurrentHashMap<>();
    }

    public Ticket saveTicket(Ticket ticket) {
        tickets.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    public Ticket getTicket(UUID ticketId) {
        return tickets.get(ticketId);
    }

    public List<Ticket> getTicketsFiltered(TicketStatus status, String userId, String assigneeId) {
        return tickets.values().stream()
                .filter(ticket ->  status == null || ticket.getStatus() == status)
                .filter(ticket -> userId == null || ticket.getUserId().equals(userId))
                .filter(ticket ->  assigneeId == null ||
                        ticket.getAssigneeId() != null && ticket.getAssigneeId().equals(assigneeId))
                .toList();
    }

    public ConcurrentHashMap<UUID, Ticket> getTickets() {
        return tickets;
    }
}
