package com.learningpurpose.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_daily_study_rhythm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyStudyRhythm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "hours_spent", nullable = false, precision = 4, scale = 2)
    private BigDecimal hoursSpent;
}
