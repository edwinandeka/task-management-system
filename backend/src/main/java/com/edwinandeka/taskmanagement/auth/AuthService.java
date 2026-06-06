package com.edwinandeka.taskmanagement.auth;

import com.edwinandeka.taskmanagement.security.JwtService;
import com.edwinandeka.taskmanagement.user.User;
import com.edwinandeka.taskmanagement.user.UserRepository;
import com.edwinandeka.taskmanagement.user.UserResponseDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(
            final AuthenticationManager authenticationManager,
            final JwtService jwtService,
            final UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponseDto login(final LoginRequestDto request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        final String accessToken = jwtService.generateToken(authentication);
        final User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.email()));

        return new LoginResponseDto(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                UserResponseDto.from(user)
        );
    }
}
