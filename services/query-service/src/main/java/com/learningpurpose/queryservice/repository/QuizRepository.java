package com.learningpurpose.queryservice.repository;

import com.learningpurpose.queryservice.document.QuizDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepository
        extends MongoRepository<QuizDocument, Long> {

    List<QuizDocument> findByCategoryId(Long categoryId);

    List<QuizDocument> findByActiveTrue();
}