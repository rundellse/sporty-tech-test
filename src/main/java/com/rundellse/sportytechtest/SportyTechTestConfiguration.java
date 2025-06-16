package com.rundellse.sportytechtest;

import com.rundellse.sportytechtest.persistence.CommentRepository;
import com.rundellse.sportytechtest.persistence.TicketRepository;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SportyTechTestConfiguration {

    private TicketRepository ticketRepository() {
        return new TicketRepository();
    }

    private CommentRepository commentRepository() {
        return new CommentRepository();
    }

}
