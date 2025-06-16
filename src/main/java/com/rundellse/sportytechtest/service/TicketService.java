package com.rundellse.sportytechtest.service;

import com.rundellse.sportytechtest.model.Comment;
import com.rundellse.sportytechtest.model.Ticket;
import com.rundellse.sportytechtest.model.TicketStatus;
import com.rundellse.sportytechtest.model.Visibility;
import com.rundellse.sportytechtest.persistence.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.rundellse.sportytechtest.model.TicketStatus.*;

@Service
public class TicketService {

    private final CommentRepository commentRepository;

    private static final Map<TicketStatus, Set<TicketStatus>> PERMITTED_NEXT_STATUSES = new HashMap<>();

    @Autowired
    public TicketService(CommentRepository commentRepository) {
        PERMITTED_NEXT_STATUSES.put(OPEN, Set.of(IN_PROGRESS, CLOSED));
        PERMITTED_NEXT_STATUSES.put(IN_PROGRESS, Set.of(RESOLVED, OPEN));
        PERMITTED_NEXT_STATUSES.put(RESOLVED, Set.of(CLOSED, IN_PROGRESS));
        PERMITTED_NEXT_STATUSES.put(CLOSED, Set.of(OPEN));

        this.commentRepository = commentRepository;
    }

    public boolean validateTicketStatusChange(TicketStatus currentStatus, TicketStatus newStatus) {
        return PERMITTED_NEXT_STATUSES.get(currentStatus).contains(newStatus);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.saveComment(comment);
    }

    public List<Comment> getFilteredCommentsForTicket(Ticket ticket) {
        List<Comment> commentsForTicket = commentRepository.getCommentsForTicket(ticket);
//      TODO: The spec mentions public comments only being available to users, currently there are no requester details available but the filter
//      would be similar to the sequence here.
//        if (requester.type == admin) { // Elsewhere we have checked against id prefix, but this is likely placeholder
//            return  commentsForTicket;
//        } else if (requester.type == user) {
//            return commentsForTicket.stream().filter(comment -> comment.getVisibility() == Visibility.PUBLIC).toList();
//        }
        return commentsForTicket != null ? commentsForTicket : Collections.emptyList();
    }

}
