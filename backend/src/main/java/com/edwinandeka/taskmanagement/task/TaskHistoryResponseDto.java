package com.edwinandeka.taskmanagement.task;

import com.edwinandeka.taskmanagement.user.UserResponseDto;

import java.time.Instant;

public record TaskHistoryResponseDto(
        Long id,
        Long taskId,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        UserResponseDto changedBy,
        Instant changedAt
) {
}
