package com.learningpurpose.queryservice.document.blog;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Data
@Document(collection = "posts")
public class Post {

    /** Mongo's native id — ObjectId, not String. */
    @Id
    private ObjectId _id;

    @Field("id")
    private Long id;

    @Field("content")
    private String content;

    @Field("posted_by")
    private String postedBy;

    /** Debezium writes this as an ISO-8601 string, not a Date. */
    @Field("created_at")
    private String createdAtRaw;

    /** Debezium writes JSONB columns as a stringified JSON array. */
    @Field("tags")
    private String tagsRaw;

    @Field("image_storage_key")
    private String imageStorageKey;

    @Field("__deleted")
    private String deleted;

    // ─── derived getters used by the service ───────────────────────────

    public Instant getCreatedAt() {
        if (createdAtRaw == null || createdAtRaw.isBlank()) return null;
        try {
            return Instant.parse(createdAtRaw);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getTags() {
        if (tagsRaw == null || tagsRaw.isBlank()) return Collections.emptyList();
        try {
            String trimmed = tagsRaw.trim();
            if (trimmed.startsWith("[")) {
                return new ObjectMapper().readValue(
                        trimmed, new TypeReference<List<String>>() {});
            }
            return List.of(trimmed);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean isDeleted() {
        return "true".equalsIgnoreCase(deleted);
    }
}
