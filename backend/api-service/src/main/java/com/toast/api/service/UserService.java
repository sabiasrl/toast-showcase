package com.toast.api.service;

import com.toast.common.dto.ApiResponse;
import com.toast.common.entity.User;

import java.util.List;

public interface UserService {
    
    ApiResponse<List<User>> getAllUsers();
    
    ApiResponse<User> getUserById(Long id);
    
    ApiResponse<User> getUserByUsername(String username);
    
    ApiResponse<User> createUser(User user);
    
    ApiResponse<User> updateUser(Long id, User user);
    
    ApiResponse<Void> deleteUser(Long id);
} 