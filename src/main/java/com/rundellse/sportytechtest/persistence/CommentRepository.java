package com.rundellse.sportytechtest.persistence;

import com.rundellse.sportytechtest.model.Comment;
import com.rundellse.sportytechtest.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CommentRepository {

    private ConcurrentHashMap<UUID, List<Comment>> ticketUUIDCommentsMap;

    @Autowired
    public CommentRepository() {
        this.ticketUUIDCommentsMap = new ConcurrentHashMap<>();
    }

    public Comment saveComment(Comment comment) {
        List<Comment> comments = ticketUUIDCommentsMap.computeIfAbsent(comment.getTicketId(), uuid -> Collections.synchronizedList(new ArrayList<>()));
        comments.add(comment);

        return comment;
    }

    public List<Comment> getCommentsForTicket(Ticket ticket) {
        return ticketUUIDCommentsMap.get(ticket.getTicketId());
    }


    public ConcurrentHashMap<UUID, List<Comment>> getTicketUUIDCommentsMap() {
        return ticketUUIDCommentsMap;
    }
}
