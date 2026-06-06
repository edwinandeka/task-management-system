package com.edwinandeka.taskmanagement.user;

import com.edwinandeka.taskmanagement.auth.RegisterRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("register persists user with encoded password and returns response without password")
    void registerPersistsUserWithEncodedPassword() {
        final RegisterRequestDto request = new RegisterRequestDto(
                "Edwin Ospina",
                "edwin@test.com",
                "Password123",
                Role.ADMIN
        );

        when(userRepository.existsByEmail("edwin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            final User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        final UserResponseDto response = userService.register(request);

        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        final User persisted = captor.getValue();
        assertThat(persisted.getEmail()).isEqualTo("edwin@test.com");
        assertThat(persisted.getFullName()).isEqualTo("Edwin Ospina");
        assertThat(persisted.getPassword()).isEqualTo("$2a$10$encoded");
        assertThat(persisted.getRole()).isEqualTo(Role.ADMIN);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("edwin@test.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("register rejects duplicated email and does not save")
    void registerRejectsDuplicatedEmail() {
        final RegisterRequestDto request = new RegisterRequestDto(
                "Edwin Ospina",
                "edwin@test.com",
                "Password123",
                Role.USER
        );

        when(userRepository.existsByEmail("edwin@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
