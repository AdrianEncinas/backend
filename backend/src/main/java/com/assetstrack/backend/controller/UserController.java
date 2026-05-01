package com.assetstrack.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.assetstrack.backend.model.dto.LoginRequest;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "Authentication & User Management", description = "Operaciones de autenticación y gestión de perfil de usuario")
public class UserController {

    private final IUserService userService;
    private final SecurityUtils securityUtils;

    public UserController(IUserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT válido para las siguientes solicitudes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Usuario o contraseña incorrectos", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserDTO userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @Operation(summary = "Obtener perfil actual", description = "Retorna la información del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Perfil obtenido correctamente",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/me")
    public UserResponse getMe() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return userService.getUser(userId);
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del perfil del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UserDTO userDto) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(userService.modifyUser(userId, userDto));
    }

    @Operation(summary = "Eliminar cuenta", description = "Elimina permanentemente la cuenta del usuario autenticado")
    @ApiResponse(responseCode = "204", description = "Cuenta eliminada correctamente", content = @Content)
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        Long userId = securityUtils.getAuthenticatedUserId();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
