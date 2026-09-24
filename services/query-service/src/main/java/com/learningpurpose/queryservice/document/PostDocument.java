package com.learningpurpose.queryservice.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "posts")
public class PostDocument {

    @Id
    private Long id;

    private String content;

    private Instant createdAt;

    private String imageStorageKey;

    private Integer likeCount;

    private String name;

    private String postedBy;

    private List<String> tags;

    private Instant updatedAt;

    private Integer viewCount;
}