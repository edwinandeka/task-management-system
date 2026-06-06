package com.edwinandeka.taskmanagement.auth;

import com.edwinandeka.taskmanagement.user.Role;
import com.edwinandeka.taskmanagement.user.UserResponseDto;
import com.edwinandeka.taskmanagement.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register returns 201 with user in body")
    void registerReturnsCreatedWithUserBody() throws Exception {
        final RegisterRequestDto request = new RegisterRequestDto(
                "Edwin Ospina",
                "edwin@test.com",
                "Password123",
                Role.ADMIN
        );
        final UserResponseDto savedUser = new UserResponseDto(1L, "Edwin Ospina", "edwin@test.com", Role.ADMIN);
        when(userService.register(any(RegisterRequestDto.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("edwin@test.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 with field details when body is invalid")
    void registerReturnsBadRequestWhenBodyIsInvalid() throws Exception {
        final String invalidBody = """
                {
                  "fullName": "",
                  "email": "not-an-email",
                  "password": "short",
                  "role": null
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.fullName").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.password").exists())
                .andExpect(jsonPath("$.details.role").exists());

        verify(userService, never()).register(any(RegisterRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 with access token")
    void loginReturnsAccessToken() throws Exception {
        final LoginRequestDto request = new LoginRequestDto("edwin@test.com", "Password123");
        final UserResponseDto user = new UserResponseDto(1L, "Edwin Ospina", "edwin@test.com", Role.ADMIN);
        final LoginResponseDto response = new LoginResponseDto("jwt-token", "Bearer", 3600L, user);
        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.email").value("edwin@test.com"));
    }
}
