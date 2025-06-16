package com.rundellse.sportytechtest.api.dto;

public record AddCommentDTO(
        String authorId,
        String content,
        String visibility
) {}
