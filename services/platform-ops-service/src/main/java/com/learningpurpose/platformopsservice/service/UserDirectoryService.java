package com.learningpurpose.platformopsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDirectoryService {

    private final DynamicDataSourceService dataSourceService;

    /** Read all enabled user emails from user_db. */
    public List<String> getAllUserEmails() {
        JdbcTemplate jdbc = dataSourceService.forDatabase("user_db");
        return jdbc.queryForList("""
            SELECT email FROM users
            WHERE enabled = true AND email IS NOT NULL AND email <> ''
            ORDER BY id
        """, String.class);
    }
}