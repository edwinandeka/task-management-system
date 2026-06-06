package com.edwinandeka.taskmanagement.auth;

import com.edwinandeka.taskmanagement.user.UserResponseDto;

public record AuthResponseDto(
        String message,
        UserResponseDto user
) {
}