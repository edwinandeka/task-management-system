package com.edwinandeka.taskmanagement.comment;

import com.edwinandeka.taskmanagement.user.UserResponseDto;

import java.time.Instant;

public record TaskCommentResponseDto(
        Long id,
        Long taskId,
        String content,
        UserResponseDto author,
        Instant createdAt
) {
}
