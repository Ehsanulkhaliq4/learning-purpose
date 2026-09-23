package com.learningpurpose.blogservice.query.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "posts_read_model")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDocument {

    @Id
    private Long id; // Mirrored from PostgreSQL primary key

    @TextIndexed
    private String name;

    @TextIndexed
    private String content;

    @Indexed
    private String postedBy;

    private String imageUrl;
    private int viewCount;
    private int likeCount;
    private List<String> tags = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private int commentCount;
}