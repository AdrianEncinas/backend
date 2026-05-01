package com.assetstrack.backend.controller;

import com.assetstrack.backend.config.JwtUtil;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.config.UserDetailsServiceImpl;
import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.model.dto.LoginRequest;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;
import com.assetstrack.backend.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

        @MockitoBean
        private SecurityUtils securityUtils;

    @Autowired
    private ObjectMapper objectMapper;

    // ── POST /login ───────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        Map<String, String> response = Map.of("token", "mock-jwt");
        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest credentials = new LoginRequest();
        credentials.setUsername("johndoe");
        credentials.setPassword("secret123");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt"));
    }

    @Test
    void login_invalidCredentials_returns404() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new NotFoundException("User or password incorrect"));

        LoginRequest credentials = new LoginRequest();
        credentials.setUsername("bad");
        credentials.setPassword("wrong");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User or password incorrect"));
    }

        // ── GET /me ───────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
        void getMe_returnsUser() throws Exception {
        UserResponse user = UserResponse.builder().id(1L).username("johndoe").build();
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(userService.getUser(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("johndoe"));
    }

        // ── GET /me (error) ───────────────────────────────────────────────────────

    @Test
    @WithMockUser
        void getMe_nonExistingId_returns500() throws Exception {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(99L);
        when(userService.getUser(99L)).thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isInternalServerError());
    }

        // ── POST /register ────────────────────────────────────────────────────────

    @Test
    void createUser_validBody_returnsCreatedUser() throws Exception {
        UserDTO input = UserDTO.builder().username("newuser").password("pass").baseCurrency("USD").build();
        UserResponse saved = UserResponse.builder().id(2L).username("newuser").baseCurrency("USD").build();
        when(userService.createUser(any(UserDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

        // ── PUT /me ───────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void modifyUser_existingId_returns200() throws Exception {
        UserDTO input = UserDTO.builder().username("modified").password("newpass").baseCurrency("EUR").build();
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(userService.modifyUser(eq(1L), any(UserDTO.class)))
                .thenReturn(UserResponse.builder().id(1L).username("modified").build());

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("modified"));
    }

        // ── DELETE /me ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void deleteUser_existingId_returns204() throws Exception {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteUser_nonExistingId_returns404() throws Exception {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(99L);
        doThrow(new NotFoundException("not found")).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/v1/users/me"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("not found"));
    }
}
