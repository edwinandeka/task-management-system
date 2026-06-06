package com.edwinandeka.taskmanagement.task;

import com.edwinandeka.taskmanagement.user.UserResponseDto;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        UserResponseDto createdBy,
        UserResponseDto assignedTo,
        Instant createdAt,
        Instant updatedAt
) {
}
