package com.rundellse.sportytechtest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rundellse.sportytechtest.model.Comment;
import com.rundellse.sportytechtest.model.Ticket;
import com.rundellse.sportytechtest.model.Visibility;
import com.rundellse.sportytechtest.persistence.CommentRepository;
import com.rundellse.sportytechtest.persistence.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;


    @BeforeEach
    public void beforeEach() {
        ticketRepository.getTickets().clear();
        commentRepository.getTicketUUIDCommentsMap().clear();
    }


    @Test
    void testCreateTicket_happyPath() throws Exception {
        var payload = """
        {
            "userId": "user-001",
            "subject": "Payment issue",
            "description": "I was charged twice for the same order."
        }
        """;

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Payment issue"))
                .andExpect(jsonPath("$.description").value("I was charged twice for the same order."))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.userId").value("user-001"))
                .andExpect(jsonPath("$.assigneeId").doesNotExist())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").value(startsWith(LocalDate.now().toString())))
                .andExpect(jsonPath("$.updatedAt").value(startsWith(LocalDate.now().toString())));

        Ticket resultTicket = ticketRepository.getTickets().values().stream().findFirst().orElseThrow(() -> new RuntimeException("New ticket not found"));
        assertThat(resultTicket.getTicketId(), isA(UUID.class));
        assertThat(resultTicket.getSubject(), is("Payment issue"));
        assertThat(resultTicket.getDescription(), is("I was charged twice for the same order."));
        assertThat(resultTicket.getStatus().name(), is("OPEN"));
        assertThat(resultTicket.getUserId(), is("user-001"));
        assertThat(resultTicket.getAssigneeId(), is(nullValue()));
        assertThat(resultTicket.getCreatedAt().toLocalDate(), is(LocalDate.now()));
        assertThat(resultTicket.getUpdatedAt().toLocalDate(), is(LocalDate.now()));
    }

    @Test
    void testListTicketsWithComments_commentsReturnedWithTicket() throws Exception {
        // Create ticket directly
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                "Test subject",
                "Test description",
                com.rundellse.sportytechtest.model.TicketStatus.OPEN,
                "user-007",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Comment comment1 = new Comment(
                UUID.randomUUID(),
                ticket.getTicketId(),
                "agent-001",
                "Agent comment",
                Visibility.PUBLIC,
                LocalDateTime.now()
        );
        Comment comment2 = new Comment(
                UUID.randomUUID(),
                ticket.getTicketId(),
                "user-007",
                "User comment",
                Visibility.PUBLIC,
                LocalDateTime.now()
        );

        ticketRepository.saveTicket(ticket);
        commentRepository.saveComment(comment1);
        commentRepository.saveComment(comment2);

        mockMvc.perform(get("/tickets?userId=user-007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comments", hasSize(2)))
                .andExpect(jsonPath("$[0].comments[*].content", containsInAnyOrder("Agent comment", "User comment")));
    }

    @Test
    void testAddComment_withIncorrectTicketId_notFoundResponse() throws Exception {
        var publicCommentPayload = """
        {
            "authorId": "user-003",
            "content": "I had this issue too!",
            "visibility": "PUBLIC"
        }
        """;
        mockMvc.perform(post("/tickets/" + UUID.randomUUID() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicCommentPayload))
                .andExpect(status().isNotFound())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    void testAddAndGetCommentVisibility_agentCanAddAnyTypeOfComment() throws Exception {
        // Create ticket
        var ticketPayload = """
        {
            "userId": "user-002",
            "subject": "Login issue",
            "description": "Can't log in."
        }
        """;
        String ticketResponse = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ticketId = objectMapper.readTree(ticketResponse).get("ticketId").asText();

        // Add public comment
        var publicCommentPayload = """
        {
            "authorId": "agent-123",
            "content": "We're investigating.",
            "visibility": "PUBLIC"
        }
        """;
        mockMvc.perform(post("/tickets/" + ticketId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicCommentPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        // Add internal comment
        var internalCommentPayload = """
        {
            "authorId": "agent-123",
            "content": "Internal note.",
            "visibility": "INTERNAL"
        }
        """;
        mockMvc.perform(post("/tickets/" + ticketId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(internalCommentPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("INTERNAL"));
    }

    @Test
    void testAddAndGetCommentVisibility_userCanOnlyAddPublicComment() throws Exception {
        // Create ticket
        var ticketPayload = """
        {
            "userId": "user-002",
            "subject": "Login issue",
            "description": "Can't log in."
        }
        """;
        String ticketResponse = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ticketId = objectMapper.readTree(ticketResponse).get("ticketId").asText();

        // Add public comment
        var publicCommentPayload = """
        {
            "authorId": "user-003",
            "content": "I had this issue too!",
            "visibility": "PUBLIC"
        }
        """;
        mockMvc.perform(post("/tickets/" + ticketId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicCommentPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        // Add internal comment
        var internalCommentPayload = """
        {
            "authorId": "user-004",
            "content": "I would greatly like for this to be looked into.",
            "visibility": "INTERNAL"
        }
        """;
        mockMvc.perform(post("/tickets/" + ticketId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(internalCommentPayload))
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    void testStatusChange_happyPath() throws Exception {
        // Create ticket
        var ticketPayload = """
        {
            "userId": "user-003",
            "subject": "Refund",
            "description": "Requesting refund."
        }
        """;
        String ticketResponse = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ticketId = objectMapper.readTree(ticketResponse).get("ticketId").asText();

        // Valid status transition: OPEN -> IN_PROGRESS
        var inProgressStatusPayload = """
        {
            "status": "IN_PROGRESS"
        }
        """;
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inProgressStatusPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Valid status transition: IN_PROGRESS -> RESOLVED
        var resolvedStatusPayload = """
        {
            "status": "RESOLVED"
        }
        """;
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolvedStatusPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        // Valid status transition: RESOLVED -> CLOSED
        var closedStatusPayload = """
        {
            "status": "CLOSED"
        }
        """;
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closedStatusPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void testStatusChange_invalidStatusChange() throws Exception {
        // Create ticket
        var ticketPayload = """
                {
                    "userId": "user-003",
                    "subject": "Refund",
                    "description": "Requesting refund."
                }
                """;
        String ticketResponse = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ticketId = objectMapper.readTree(ticketResponse).get("ticketId").asText();

        // non-valid status transition: OPEN -> RESOLVED
        var inProgressStatusPayload = """
                {
                    "status": "RESOLVED"
                }
                """;
        mockMvc.perform(patch("/tickets/" + ticketId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inProgressStatusPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    void testListTicketsWithFilters_onlyMatchingUserReturned() throws Exception {
        // Create two tickets with different users and status
        var ticket1 = """
        {
            "userId": "user-004",
            "subject": "A",
            "description": "desc"
        }
        """;
        var ticket2 = """
        {
            "userId": "user-005",
            "subject": "B",
            "description": "desc"
        }
        """;
        mockMvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(ticket1)).andExpect(status().isCreated());
        mockMvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(ticket2)).andExpect(status().isCreated());

        // List tickets for user-004
        mockMvc.perform(get("/tickets?userId=user-004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is("user-004")))
                .andExpect(jsonPath("$[0].subject", is("A")));
    }

    @Test
    void testListTicketsWithStatusFilter_onlyMatchingStatusReturned() throws Exception {
        // Create tickets with different statuses
        var ticket1 = """
        {
            "userId": "user-010",
            "subject": "Subject 1",
            "description": "desc"
        }
        """;
        var ticket2 = """
        {
            "userId": "user-011",
            "subject": "Subject 2",
            "description": "desc"
        }
        """;
        // Create both tickets
        String ticket1Response = mockMvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(ticket1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ticket2Response = mockMvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(ticket2))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String ticket1Id = objectMapper.readTree(ticket1Response).get("ticketId").asText();
        String ticket2Id = objectMapper.readTree(ticket2Response).get("ticketId").asText();

        // Change ticket2 status to IN_PROGRESS
        var statusPayload = """
        {
            "status": "IN_PROGRESS"
        }
        """;
        mockMvc.perform(patch("/tickets/" + ticket2Id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload))
                .andExpect(status().isOk());

        // List tickets with status=IN_PROGRESS
        mockMvc.perform(get("/tickets?status=IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is("user-011")))
                .andExpect(jsonPath("$[0].status", is("IN_PROGRESS")));
    }

    @Test
    void testListTicketsWithStatusAndUserFilter_onlyMatchingTicketReturned() throws Exception {
        // Create tickets with different users and statuses
        var ticket1 = """
        {
            "userId": "user-020",
            "subject": "Subject 1",
            "description": "desc"
        }
        """;
        var ticket2 = """
        {
            "userId": "user-021",
            "subject": "Subject 2",
            "description": "desc"
        }
        """;
        // Create both tickets
        String ticket1Response = mockMvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(ticket1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ticket2Response = mockMvc.perform(post("/tickets").contentType(MediaType.APPLICATION_JSON).content(ticket2))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String ticket1Id = objectMapper.readTree(ticket1Response).get("ticketId").asText();
        String ticket2Id = objectMapper.readTree(ticket2Response).get("ticketId").asText();

        // Change ticket2 status to IN_PROGRESS
        var statusPayload = """
        {
            "status": "IN_PROGRESS"
        }
        """;
        mockMvc.perform(patch("/tickets/" + ticket2Id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusPayload))
                .andExpect(status().isOk());

        // List tickets with status=IN_PROGRESS and userId=user-021
        mockMvc.perform(get("/tickets?status=IN_PROGRESS&userId=user-021"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is("user-021")))
                .andExpect(jsonPath("$[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$[0].subject", is("Subject 2")));
    }
}
