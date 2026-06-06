package com.edwinandeka.taskmanagement.task;

import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateDto(
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}
