package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.model.User;

public interface UserService {
    User getUserByUsername(String username);
    void deleteUser(Long userId);
}
