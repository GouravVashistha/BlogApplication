package com.blog_app_apis.service;

import com.blog_app_apis.dtos.UserDTO;
import com.blog_app_apis.exceptions.InvalidMailException;

import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO user) throws InvalidMailException;

    public UserDTO updateUser(UserDTO userDTO, Integer userId) throws InvalidMailException;

    UserDTO getUserById(Integer userId);

    List<UserDTO> getAllUsers();

    void deleteUser(Integer userId);

    UserDTO registerNewUser(UserDTO userDto);

    UserDTO assignAdminRole(Integer userId);
}
