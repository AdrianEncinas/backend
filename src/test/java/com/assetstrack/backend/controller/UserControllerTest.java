package com.assetstrack.backend.controller;

import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.assetstrack.backend.exception.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Autowired
    private ObjectMapper objectMapper;

    // ── POST /login ───────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        Map<String, String> response = Map.of("token", "session-1", "user", "johndoe");
        when(userService.login(anyMap())).thenReturn(response);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "johndoe");
        credentials.put("password", "secret123");

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void login_invalidCredentials_propagatesNotFoundException() throws Exception {
        when(userService.login(anyMap())).thenThrow(new NotFoundException("User or password incorrect"));

        Map<String, String> credentials = Map.of("username", "bad", "password", "wrong");

        assertThatThrownBy(() ->
                mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials))))
                .hasMessageContaining("incorrect");
    }

    // ── GET /list ─────────────────────────────────────────────────────────────

    @Test
    void getListUsers_returnsList() throws Exception {
        UserDTO user = UserDTO.builder().id(1L).username("johndoe").build();
        when(userService.getUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("johndoe"));
    }

    // ── GET /get/{id} ─────────────────────────────────────────────────────────

    @Test
    void getUserById_existingId_returnsUser() throws Exception {
        UserDTO user = UserDTO.builder().id(1L).username("johndoe").build();
        when(userService.getUser(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/user/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void getUserById_nonExistingId_propagatesException() throws Exception {
        when(userService.getUser(99L)).thenThrow(new RuntimeException("Usuario no encontrado"));

        assertThatThrownBy(() ->
                mockMvc.perform(get("/api/v1/user/get/99")))
                .hasMessageContaining("no encontrado");
    }

    // ── POST /create ──────────────────────────────────────────────────────────

    @Test
    void createUser_validBody_returnsCreatedUser() throws Exception {
        UserDTO input = UserDTO.builder().username("newuser").password("pass").baseCurrency("USD").build();
        UserDTO saved = UserDTO.builder().id(2L).username("newuser").password("pass").baseCurrency("USD").build();
        when(userService.createUser(any(UserDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    // ── PUT /modify/{id} ──────────────────────────────────────────────────────

    @Test
    void modifyUser_existingId_returns200() throws Exception {
        UserDTO input = UserDTO.builder().username("modified").password("newpass").baseCurrency("EUR").build();
        when(userService.modifyUser(eq(1L), any(UserDTO.class)))
                .thenReturn(UserDTO.builder().id(1L).username("modified").build());

        mockMvc.perform(put("/api/v1/user/modify/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().string("User modified"));
    }

    // ── DELETE /delete/{id} ───────────────────────────────────────────────────

    @Test
    void deleteUser_existingId_returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/user/delete/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_nonExistingId_propagatesNotFoundException() throws Exception {
        doThrow(new NotFoundException("not found")).when(userService).deleteUser(99L);

        assertThatThrownBy(() ->
                mockMvc.perform(delete("/api/v1/user/delete/99")))
                .hasMessageContaining("not found");
    }
}
