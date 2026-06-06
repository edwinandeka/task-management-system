package com.edwinandeka.taskmanagement.user;

public record UserResponseDto(
        Long id,
        String fullName,
        String email,
        Role role
) {
}