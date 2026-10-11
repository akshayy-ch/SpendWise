package com.spendwise.service;

import com.spendwise.dto.request.user.UpdateUserRoleRequest;
import com.spendwise.dto.response.user.AdminUserResponse;
import com.spendwise.dto.response.user.UserProfileResponse;
import com.spendwise.dto.response.user.UserSearchResponse;
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
    private final CurrentUserService currentUserService;

    public UserProfileResponse getMyProfile() {
        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new UserDoesNotExist("User does not exist"));

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

    public List<UserSearchResponse> searchUsers(String query) {
        if (query == null || query.trim().length() < 2) return List.of();

        return userRepository.findTop10ByUsernameContainingIgnoreCase(query.trim())
                .stream()
                .map(user -> UserSearchResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .build())
                .toList();
    }

    public AdminUserResponse getUser(UUID userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserDoesNotExist("User does not exist"));
        return userMapper.toResponse(user);
    }

    public AdminUserResponse updateUserRole(UUID userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserDoesNotExist("User does not exist"));
        if (user.getRole() == Role.ADMIN) throw new InvalidRoleChangeException("User is already an admin");
        if (request.getRole() != Role.ADMIN) throw new InvalidRoleChangeException("An admin role cannot be changed to user");
        user.setRole(Role.ADMIN);
        return userMapper.toResponse(userRepository.save(user));
    }

    public List<AdminUserResponse> getUsers(){
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }
}