package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountReactivationScheduler {

    private final UserRepository userRepository;

    @Scheduled(fixedDelayString = "${app.accounts.reactivation-check-ms:60000}")
    public void reactivateExpiredAccounts() {
        int reactivatedCount = userRepository.reactivateExpiredAccounts(Instant.now());
        if (reactivatedCount > 0) {
            log.info("Automatically reactivated {} expired user account(s)", reactivatedCount);
        }
    }
}
