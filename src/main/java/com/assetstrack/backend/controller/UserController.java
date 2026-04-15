package com.assetstrack.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.service.IUserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        userService.login(credentials);
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/list")
    public List<UserDTO> getListUsers() {
        return userService.getUsers();
    }

    @GetMapping("/get/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping("/create") // Este servirá como tu "Register"
    public UserDTO createUser(@RequestBody UserDTO userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/modify/{id}")
    public ResponseEntity<String> modifyUser(@PathVariable Long id, @RequestBody UserDTO userDto) {
        userService.modifyUser(id, userDto);
        return ResponseEntity.ok("User modified");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}