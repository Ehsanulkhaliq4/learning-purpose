package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSummary>> searchUsers(@RequestParam String query) {
        if (!StringUtils.hasText(query)) {
            return ResponseEntity.ok(List.of());
        }

        List<UserSummary> users = userService.searchUsersByUsername(query.trim()).stream()
                .map(UserSummary::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-id/{userId}")
    public ResponseEntity<UserSummary> getUserSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(UserSummary.from(userService.getUserById(userId)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    public record UserSummary(Long id, String username, String firstName, String lastName) {
        private static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName());
        }
    }
}
