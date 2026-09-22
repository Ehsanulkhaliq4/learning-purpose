package com.learningpurpose.queryservice.document.blog;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "comments")
public class Comment {

    @Id
    private String _id;

    @Field("id")
    private Long id;

    @Field("post_id")
    private Long postId;

    @Field("posted_by")
    private String postedBy;

    @Field("__deleted")
    private String deleted;

    public boolean isDeleted() {
        return "true".equalsIgnoreCase(deleted);
    }
}
