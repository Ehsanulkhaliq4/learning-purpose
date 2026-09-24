package com.learningpurpose.queryservice.repository.blog;

import com.learningpurpose.queryservice.document.blog.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends MongoRepository<Comment, ObjectId> {

    @Query("{ 'post_id': ?0, '__deleted': { $ne: 'true' } }")
    Page<Comment> findActiveByPostId(Long postId, Pageable pageable);

    @Query(value = "{ 'post_id': ?0, '__deleted': { $ne: 'true' } }", count = true)
    long countActiveByPostId(Long postId);
}
