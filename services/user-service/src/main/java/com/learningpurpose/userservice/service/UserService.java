package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.model.User;

import java.util.List;

public interface UserService {
    User getUserByUsername(String username);
    User getUserById(Long userId);
    List<User> searchUsersByUsername(String query);
    void deleteUser(Long userId);
}
