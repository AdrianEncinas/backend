package com.assetstrack.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.service.IUserService;

@RestController
@RequestMapping("/api/v1/user")
// Importante: No pongas @CrossOrigin aquí si ya tienes el CorsConfig que hicimos antes
public class UserController {

    @Autowired
    IUserService userService;

    // --- NUEVO: MÉTODO DE LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Buscamos al usuario (esto es lo que generaba tu log de Hibernate)
        UserDTO user = userService.getUsers().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            // Creamos la respuesta que espera tu AuthContext.tsx de React
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", "session-" + user.getId()); // Token temporal
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Usuario o contraseña incorrectos"));
        }
    }

    @GetMapping("/list")
    public List<UserDTO> getListUsers() {
        return userService.getUsers();
    }

    @GetMapping("/get/{id}")
    public UserDTO getMethodName(@PathVariable Long id) {
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