package com.assetstrack.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.assetstrack.backend.config.JwtUtil;
import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.mapper.Mapper;
import com.assetstrack.backend.model.dto.LoginRequest;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.repository.UserRepository;

@Service
public class UserService implements IUserService{

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public List<UserResponse> getUsers() {
        return userRepo.findAll().stream().map(Mapper::toResponse).toList();
    }

    @Override
    public UserResponse getUser(Long id){
        

        return userRepo.findById(id).map(Mapper::toResponse)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    public UserResponse createUser(UserDTO userDto) {
        userDto.setId(null);
        userRepo.findByUsername(userDto.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });
        User user = User.builder()
            .username(userDto.getUsername())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .baseCurrency(userDto.getBaseCurrency())
            .build();
        return Mapper.toResponse(userRepo.save(user));
    }

    @Override
    public UserResponse modifyUser(Long id, UserDTO userDto) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        userRepo.findByUsername(userDto.getUsername()).ifPresent(u -> {
            if (!u.getId().equals(id)) {
                throw new IllegalArgumentException("Username already exists");
            }
        });

        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setBaseCurrency(userDto.getBaseCurrency());
        
        return Mapper.toResponse(userRepo.save(user));
    }

    @Override
    public void deleteUser(long id) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        userRepo.delete(user);
    }

    @Override
    public Map<String, String> login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        String token = jwtUtil.generateToken(loginRequest.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

}

