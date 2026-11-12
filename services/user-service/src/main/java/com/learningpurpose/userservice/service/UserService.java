package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.dto.UpdateUserProfileRequest;
import com.learningpurpose.userservice.dto.UserProfileDto;
import com.learningpurpose.userservice.model.User;

import java.util.List;

public interface UserService {
    User getUserByUsername(String username);
    User getUserById(Long userId);
    List<User> searchUsersByUsername(String query);
    void deleteUser(Long userId);
    UserProfileDto getMyProfile(String username);
    UserProfileDto updateMyProfile(String username, UpdateUserProfileRequest request);
    void deactivateMyAccount(String username);
}
