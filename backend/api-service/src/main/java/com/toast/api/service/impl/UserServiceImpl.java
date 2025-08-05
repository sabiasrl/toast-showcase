package com.toast.api.service.impl;

import com.toast.api.service.UserService;
import com.toast.common.dto.ApiResponse;
import com.toast.common.entity.User;
import com.toast.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ApiResponse.success("Users retrieved successfully", users);
    }

    @Override
    public ApiResponse<User> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> ApiResponse.success("User found", user))
                .orElse(ApiResponse.error("User not found"));
    }

    @Override
    public ApiResponse<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> ApiResponse.success("User found", user))
                .orElse(ApiResponse.error("User not found"));
    }

    @Override
    public ApiResponse<User> createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ApiResponse.error("Username already exists");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            return ApiResponse.error("Email already exists");
        }
        
        User savedUser = userRepository.save(user);
        return ApiResponse.success("User created successfully", savedUser);
    }

    @Override
    public ApiResponse<User> updateUser(Long id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setIsActive(user.getIsActive());
                    existingUser.setRole(user.getRole());
                    
                    User updatedUser = userRepository.save(existingUser);
                    return ApiResponse.success("User updated successfully", updatedUser);
                })
                .orElse(ApiResponse.error("User not found"));
    }

    @Override
    public ApiResponse<Void> deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ApiResponse.success("User deleted successfully", null);
        }
        return ApiResponse.error("User not found");
    }
} 