package com.learningpurpose.queryservice.repository;

import com.learningpurpose.queryservice.document.ExamCategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExamCategoryRepository
        extends MongoRepository<ExamCategoryDocument, Long> {
}