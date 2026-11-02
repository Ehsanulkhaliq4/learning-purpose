package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.UserDailyStudyRhythm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyStudyRhythmRepository extends JpaRepository<UserDailyStudyRhythm, Long> {
    List<UserDailyStudyRhythm> findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(Long userId, LocalDate startDate, LocalDate endDate);
    Optional<UserDailyStudyRhythm> findByUserIdAndStudyDate(Long userId, LocalDate studyDate);
}
