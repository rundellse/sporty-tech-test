package com.rundellse.sportytechtest.api.dto;

public record CreateTicketDTO(
        String userId,
        String subject,
        String description
) {}
