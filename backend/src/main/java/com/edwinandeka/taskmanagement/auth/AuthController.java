package com.edwinandeka.taskmanagement.auth;

import com.edwinandeka.taskmanagement.user.UserResponseDto;
import com.edwinandeka.taskmanagement.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody final RegisterRequestDto request) {
        final UserResponseDto user = userService.register(request);

        return new AuthResponseDto(
                "User registered successfully",
                user
        );
    }
}