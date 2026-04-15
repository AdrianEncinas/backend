package com.assetstrack.backend.service;

import java.util.List;
import java.util.Map;

import com.assetstrack.backend.model.dto.UserDTO;

public interface IUserService {

    Map<String, String> login(Map<String, String> credentials);
    List<UserDTO> getUsers();
    UserDTO getUser(Long id);
    UserDTO createUser(UserDTO userDto);
    UserDTO modifyUser(Long id, UserDTO userDto);
    void deleteUser(long id);
    
}
