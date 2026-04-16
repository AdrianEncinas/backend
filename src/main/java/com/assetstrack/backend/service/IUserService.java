package com.assetstrack.backend.service;

import java.util.List;
import java.util.Map;

import com.assetstrack.backend.model.dto.LoginRequest;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.dto.UserResponse;

public interface IUserService {

    Map<String, String> login(LoginRequest loginRequest);
    List<UserResponse> getUsers();
    UserResponse getUser(Long id);
    UserResponse createUser(UserDTO userDto);
    UserResponse modifyUser(Long id, UserDTO userDto);
    void deleteUser(long id);
    
}
