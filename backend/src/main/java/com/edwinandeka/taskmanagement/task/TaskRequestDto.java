package com.edwinandeka.taskmanagement.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequestDto(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must not exceed 160 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Priority is required")
        TaskPriority priority,

        LocalDate dueDate,

        Long assignedToId
) {
}
