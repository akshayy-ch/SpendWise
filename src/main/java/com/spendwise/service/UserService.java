package com.spendwise.service;

import com.spendwise.dto.request.user.UpdateUserRoleRequest;
import com.spendwise.dto.response.user.AdminUserResponse;
import com.spendwise.entity.User;
import com.spendwise.enums.Role;
import com.spendwise.exception.GroupMemberExceptions.UserDoesNotExist;
import com.spendwise.exception.UserExceptions.InvalidRoleChangeException;
import com.spendwise.mapper.UserMapper;
import com.spendwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AdminUserResponse getUser(UUID userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new UserDoesNotExist("User does not exist"));

        return userMapper.toResponse(user);
    }

    public AdminUserResponse updateUserRole(UUID userId, UpdateUserRoleRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserDoesNotExist("User does not exist"));

        if (user.getRole() == Role.ADMIN) {
            throw new InvalidRoleChangeException("User is already an admin");
        }

        if (request.getRole() != Role.ADMIN) {
            throw new InvalidRoleChangeException("An admin role cannot be changed to user");
        }

        user.setRole(Role.ADMIN);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }
    public List<AdminUserResponse> getUsers(){

        List<User> users = userRepository.findAll();

        return users.stream().map(userMapper::toResponse).toList();
    }
}
