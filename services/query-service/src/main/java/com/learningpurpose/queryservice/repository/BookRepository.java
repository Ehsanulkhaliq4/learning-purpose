package com.learningpurpose.queryservice.repository;

import com.learningpurpose.queryservice.document.BookDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository
        extends MongoRepository<BookDocument, Long> {
}