package com.learningpurpose.userservice.config;

import com.learningpurpose.userservice.model.Role;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.repository.RoleRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializerConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    @Value("${app.security.admin.email}")
    private String adminEmail;

    @Value("${app.security.admin.first-name}")
    private String firstName;

    @Value("${app.security.admin.last-name}")
    private String lastName;

    @Value("${app.security.admin.phone}")
    private String phone;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (userRepository.existsByUsername(adminUsername)){
            log.info("Admin account '{}' already initialized. Skipping creation.", adminUsername);
            return;
        }
        log.info("Admin account not detected. Initializing default system administrator...");

        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN").orElseGet(() -> roleRepository.save(Role.builder().roleName("ROLE_ADMIN").build()));
        System.out.println("adminRole : "+adminRole);
        User adminUser = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        adminUser.setProfile("software-engineer.png");
        User savedUser = userRepository.save(adminUser);
        log.info("System administrator '{}' successfully bootstrapped.", adminUsername);
    }
}
