package com.learningpurpose.queryservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.learningpurpose.queryservice.document.PostDocument;
import com.learningpurpose.queryservice.dto.DebeziumEvent;
import com.learningpurpose.queryservice.repository.PostRepository;

@Slf4j
@Component
@RequiredArgsConstructor 
public class PostCdcConsumer {

    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;

    @KafkaListener(
            topics = "blog.public.posts",
            groupId = "query-service"
    )
    public void consume(@Payload(required = false) String message) {
        if (message == null) {
            log.warn("Received null message from Kafka topic 'blog.public.posts'");
            return;
        }
         try {
            DebeziumEvent event =
                    objectMapper.readValue(message, DebeziumEvent.class);

            log.info("CDC Operation: {}", event.getOp());
            switch (event.getOp()) {
                case "r", "c", "u" -> saveOrUpdatePost(event);
                case "d" -> deletePost(event);
                default -> log.warn("Unhandled CDC operation: {}", event.getOp());
        
            }
            log.info("Before: {}", event.getBefore());
            log.info("After: {}", event.getAfter());

        } catch (Exception e) {
            log.error("Failed to parse CDC event", e);
        }
    }

    private void saveOrUpdatePost(DebeziumEvent event) throws Exception {

    if (event.getAfter() == null || event.getAfter().isNull()) {
        return;
    }

    JsonNode after = event.getAfter();

    PostDocument post = new PostDocument();

    post.setId(after.get("id").asLong());
    post.setContent(after.get("content").asString());

    if (!after.get("created_at").isNull()) {
        post.setCreatedAt(
                java.time.Instant.parse(
                        after.get("created_at").asString()
                )
        );
    }

    if (!after.get("image_storage_key").isNull()) {
        post.setImageStorageKey(
                after.get("image_storage_key").asString()
        );
    }

    post.setLikeCount(after.get("like_count").asInt());
    post.setName(after.get("name").asString());
    post.setPostedBy(after.get("posted_by").asString());
    String tagsJson = after.get("tags").asString();
    post.setTags(
            objectMapper.readValue(
                    tagsJson,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(
                                    java.util.List.class,
                                    String.class
                            )
            )
    );

    if (!after.get("updated_at").isNull()) {
        post.setUpdatedAt(
                java.time.Instant.parse(
                        after.get("updated_at").asString()
                )
        );
    }

    post.setViewCount(after.get("view_count").asInt());

    postRepository.save(post);

    log.info("Post saved to MongoDB. id={}", post.getId());
}

    private void deletePost(DebeziumEvent event) {
        if(event.getBefore() == null || event.getBefore().isNull()) {
            log.warn("Received CDC event with null 'before' field, skipping delete");
            return;
        }

       Long id = event.getBefore().get("id").asLong();
       postRepository.deleteById(id);
        log.info("Post deleted from MongoDB. id={}",id);
    }
}