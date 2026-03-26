package com.assetstrack.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.mapper.Mapper;
import com.assetstrack.backend.model.dto.UserDTO;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.repository.UserRepository;

@Service
public class UserService implements IUserService{

    @Autowired
    UserRepository userRepo;

    @Override
    public List<UserDTO> getUsers() {
        return(userRepo.findAll().stream().map(Mapper::toDTO).toList());
    }

    @Override
    public UserDTO getUser(Long id){
        UserDTO user = userRepo.findById(id).map(Mapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user;
    }

    @Override
    public UserDTO createUser(UserDTO userDto) {
        userDto.setId(null);
        User user = User.builder()
            .username(userDto.getUsername())
            .password(userDto.getPassword())
            .baseCurrency(userDto.getBaseCurrency())
            .build();
        return Mapper.toDTO(userRepo.save(user));
    }

    @Override
    public UserDTO modifyUser(Long id, UserDTO userDto) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setBaseCurrency(userDto.getBaseCurrency());
        
        return Mapper.toDTO(userRepo.save(user));
    }

    @Override
    public void deleteUser(long id) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        userRepo.delete(user);
    }

}
