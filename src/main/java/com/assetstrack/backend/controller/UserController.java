package com.assetstrack.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/api/v1/user")
@Validated
public class UserController {

    IUserService userService;
    SecurityUtils securityUtils;

    public UserController(IUserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @GetMapping("/list")
    public List<UserResponse> getListUsers() {
        return userService.getUsers();
    }

    @GetMapping("/get/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        securityUtils.verifyOwnership(id);
        return userService.getUser(id);
    }

    @PostMapping("/create")
    public UserResponse createUser(@Valid @RequestBody UserDTO userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/modify/{id}")
    public ResponseEntity<String> modifyUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDto) {
        securityUtils.verifyOwnership(id);
        userService.modifyUser(id, userDto);
        return ResponseEntity.ok("User modified");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        securityUtils.verifyOwnership(id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}