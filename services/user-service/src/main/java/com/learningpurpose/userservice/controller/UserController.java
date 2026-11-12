package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.dto.UpdateUserProfileRequest;
import com.learningpurpose.userservice.dto.UserProfileDto;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication authentication) {
        return noStore(userService.getMyProfile(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return noStore(userService.updateMyProfile(authentication.getName(), request));
    }

    @PostMapping("/me/deactivate")
    public ResponseEntity<Void> deactivateMyAccount(Authentication authentication) {
        userService.deactivateMyAccount(authentication.getName());
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }

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

    private ResponseEntity<UserProfileDto> noStore(UserProfileDto profile) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate().cachePrivate())
                .header("Pragma", "no-cache")
                .body(profile);
    }
}
