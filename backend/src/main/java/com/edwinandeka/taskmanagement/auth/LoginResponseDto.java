package com.edwinandeka.taskmanagement.auth;

import com.edwinandeka.taskmanagement.user.UserResponseDto;

public record LoginResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) {
}
