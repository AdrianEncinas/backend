package com.assetstrack.backend.service;

import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private UserService userService;

    private User userEntity;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userEntity = User.builder()
                .id(1L)
                .username("johndoe")
                .password("secret123")
                .baseCurrency("USD")
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .username("johndoe")
                .password("secret123")
                .baseCurrency("USD")
                .build();
    }

    // ── getUsers ──────────────────────────────────────────────────────────────

    @Test
    void getUsers_returnsListOfUserDTOs() {
        when(userRepo.findAll()).thenReturn(List.of(userEntity));

        List<UserDTO> result = userService.getUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("johndoe");
        verify(userRepo).findAll();
    }

    @Test
    void getUsers_emptyRepository_returnsEmptyList() {
        when(userRepo.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.getUsers();

        assertThat(result).isEmpty();
    }

    // ── getUser ───────────────────────────────────────────────────────────────

    @Test
    void getUser_existingId_returnsUserDTO() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDTO result = userService.getUser(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void getUser_nonExistingId_throwsRuntimeException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_savesAndReturnsDTO() {
        UserDTO input = UserDTO.builder()
                .id(5L) // debe ser ignorado (seteado a null)
                .username("newuser")
                .password("pass")
                .baseCurrency("EUR")
                .build();

        User savedEntity = User.builder()
                .id(2L)
                .username("newuser")
                .password("pass")
                .baseCurrency("EUR")
                .build();

        when(userRepo.save(any(User.class))).thenReturn(savedEntity);

        UserDTO result = userService.createUser(input);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        // El id del DTO de entrada debe haber sido nulificado antes del save
        verify(userRepo).save(any(User.class));
    }

    // ── modifyUser ────────────────────────────────────────────────────────────

    @Test
    void modifyUser_existingId_updatesAndReturnsDTO() {
        UserDTO modifyInput = UserDTO.builder()
                .username("modified")
                .password("newpass")
                .baseCurrency("GBP")
                .build();

        User updatedEntity = User.builder()
                .id(1L)
                .username("modified")
                .password("newpass")
                .baseCurrency("GBP")
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepo.save(any(User.class))).thenReturn(updatedEntity);

        UserDTO result = userService.modifyUser(1L, modifyInput);

        assertThat(result.getUsername()).isEqualTo("modified");
        assertThat(result.getBaseCurrency()).isEqualTo("GBP");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void modifyUser_nonExistingId_throwsNotFoundException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.modifyUser(99L, userDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no encontrado");
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void deleteUser_existingId_deletesUser() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(userEntity));

        userService.deleteUser(1L);

        verify(userRepo).delete(userEntity);
    }

    @Test
    void deleteUser_nonExistingId_throwsNotFoundException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no encontrado");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsTokenAndUser() {
        when(userRepo.findAll()).thenReturn(List.of(userEntity));

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "johndoe");
        credentials.put("password", "secret123");

        Map<String, String> result = userService.login(credentials);

        assertThat(result).containsKey("token");
        assertThat(result).containsKey("user");
        assertThat(result.get("token")).startsWith("session-");
    }

    @Test
    void login_wrongPassword_throwsNotFoundException() {
        when(userRepo.findAll()).thenReturn(List.of(userEntity));

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "johndoe");
        credentials.put("password", "wrongpass");

        assertThatThrownBy(() -> userService.login(credentials))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    void login_wrongUsername_throwsNotFoundException() {
        when(userRepo.findAll()).thenReturn(List.of(userEntity));

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "unknown");
        credentials.put("password", "secret123");

        assertThatThrownBy(() -> userService.login(credentials))
                .isInstanceOf(NotFoundException.class);
    }
}
