package com.learningpurpose.queryservice.repository.blog;

import com.learningpurpose.queryservice.document.blog.Post;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, ObjectId> {

    @Query("{ '__deleted': { $ne: 'true' } }")
    Page<Post> findActivePosts(Pageable pageable);

    @Query("{ 'id': ?0, '__deleted': { $ne: 'true' } }")
    Optional<Post> findActiveById(Long id);

    @Query("{ 'posted_by': ?0, '__deleted': { $ne: 'true' } }")
    Page<Post> findActiveByPostedBy(String postedBy, Pageable pageable);

    @Query("{ 'tags': { $regex: ?0, $options: 'i' }, '__deleted': { $ne: 'true' } }")
    Page<Post> findActiveByTag(String tag, Pageable pageable);
}
