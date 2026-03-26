package com.assetstrack.backend.service;

import java.util.List;

import com.assetstrack.backend.model.dto.UserDTO;

public interface IUserService {

    List<UserDTO> getUsers();
    UserDTO getUser(Long id);
    UserDTO createUser(UserDTO userDto);
    UserDTO modifyUser(Long id, UserDTO userDto);
    void deleteUser(long id);
    
}
