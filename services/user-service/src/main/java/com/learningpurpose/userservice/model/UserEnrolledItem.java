package com.learningpurpose.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "user_enrolled_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEnrolledItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "progress_percentage", nullable = false)
    private int progressPercentage;

    @Column(nullable = false)
    private String status;

    @Column(name = "action_text", nullable = false)
    private String actionText;

    @Column(name = "action_url", nullable = false)
    private String actionUrl;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
