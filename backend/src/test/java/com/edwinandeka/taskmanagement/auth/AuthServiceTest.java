package com.edwinandeka.taskmanagement.auth;

import com.edwinandeka.taskmanagement.security.JwtService;
import com.edwinandeka.taskmanagement.user.Role;
import com.edwinandeka.taskmanagement.user.User;
import com.edwinandeka.taskmanagement.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login returns token and user response when credentials are valid")
    void loginReturnsTokenWhenCredentialsAreValid() {
        final LoginRequestDto request = new LoginRequestDto("edwin@test.com", "Password123");
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                "edwin@test.com",
                "Password123",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        final User user = User.builder()
                .id(1L)
                .fullName("Edwin Ospina")
                .email("edwin@test.com")
                .password("$2a$10$encoded")
                .role(Role.ADMIN)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(userRepository.findByEmail("edwin@test.com")).thenReturn(Optional.of(user));

        final LoginResponseDto response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().email()).isEqualTo("edwin@test.com");
        assertThat(response.user().role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("login propagates BadCredentialsException when authentication fails")
    void loginPropagatesBadCredentialsException() {
        final LoginRequestDto request = new LoginRequestDto("edwin@test.com", "wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
        verify(userRepository, never()).findByEmail(any());
    }
}
