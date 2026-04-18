package com.assetstrack.backend.service;

import com.assetstrack.backend.config.JwtUtil;
import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.model.dto.LoginRequest;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

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
    void getUsers_returnsListOfUserResponses() {
        when(userRepo.findAll()).thenReturn(List.of(userEntity));

        List<UserResponse> result = userService.getUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("johndoe");
        verify(userRepo).findAll();
    }

    @Test
    void getUsers_emptyRepository_returnsEmptyList() {
        when(userRepo.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getUsers();

        assertThat(result).isEmpty();
    }

    // ── getUser ───────────────────────────────────────────────────────────────

    @Test
    void getUser_existingId_returnsUserResponse() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(userEntity));

        UserResponse result = userService.getUser(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void getUser_nonExistingId_throwsNotFoundException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_savesAndReturnsResponse() {
        UserDTO input = UserDTO.builder()
                .id(5L)
                .username("newuser")
                .password("pass")
                .baseCurrency("EUR")
                .build();

        User savedEntity = User.builder()
                .id(2L)
                .username("newuser")
                .password("encoded_pass")
                .baseCurrency("EUR")
                .build();

        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded_pass");
        when(userRepo.save(any(User.class))).thenReturn(savedEntity);

        UserResponse result = userService.createUser(input);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepo).save(any(User.class));
    }

    // ── modifyUser ────────────────────────────────────────────────────────────

    @Test
    void modifyUser_existingId_updatesAndReturnsResponse() {
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
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("newpass");
        when(userRepo.save(any(User.class))).thenReturn(updatedEntity);

        UserResponse result = userService.modifyUser(1L, modifyInput);

        assertThat(result.getUsername()).isEqualTo("modified");
        assertThat(result.getBaseCurrency()).isEqualTo("GBP");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void modifyUser_nonExistingId_throwsNotFoundException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.modifyUser(99L, userDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
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
                .hasMessageContaining("User not found");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsToken() {
        when(jwtUtil.generateToken("johndoe")).thenReturn("mock-jwt-token");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("secret123");

        Map<String, String> result = userService.login(loginRequest);

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo("mock-jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_wrongCredentials_throwsBadCredentialsException() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("wrongpass");

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_wrongUsername_throwsUsernameNotFoundException() {
        doThrow(new UsernameNotFoundException("User not found"))
                .when(authenticationManager).authenticate(any());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("unknown");
        loginRequest.setPassword("secret123");

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
