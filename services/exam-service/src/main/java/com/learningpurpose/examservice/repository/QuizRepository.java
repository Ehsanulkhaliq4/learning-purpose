package com.learningpurpose.examservice.repository;

import com.learningpurpose.examservice.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCategoryId(Long categoryId);
    List<Quiz> findByActiveTrue();
    List<Quiz> findByCategoryIdAndActiveTrue(Long categoryId);
}