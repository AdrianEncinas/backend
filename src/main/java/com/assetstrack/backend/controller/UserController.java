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

import com.assetstrack.backend.model.dto.LoginRequest;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final IUserService userService;
    private final SecurityUtils securityUtils;

    public UserController(IUserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserDTO userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @GetMapping("/me")
    public UserResponse getMe() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return userService.getUser(userId);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UserDTO userDto) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(userService.modifyUser(userId, userDto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        Long userId = securityUtils.getAuthenticatedUserId();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
