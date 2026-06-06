package com.edwinandeka.taskmanagement.user;

public record UserResponseDto(
        Long id,
        String fullName,
        String email,
        Role role
) {

    public static UserResponseDto from(final User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
