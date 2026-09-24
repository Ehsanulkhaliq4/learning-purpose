package com.learningpurpose.queryservice.repository;

import com.learningpurpose.queryservice.document.PostDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<PostDocument, Long> {
}