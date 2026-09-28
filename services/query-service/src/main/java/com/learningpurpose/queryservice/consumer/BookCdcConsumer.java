package com.learningpurpose.queryservice.consumer;

import com.learningpurpose.queryservice.document.BookDocument;
import com.learningpurpose.queryservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookCdcConsumer {

    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;

    @KafkaListener(
            topics = "book.public.book_catalog",
            groupId = "query-service"
    )
    public void consume(String message) {

        if (message == null) {
            log.warn("Received null book CDC message");
            return;
        }

        try {

            JsonNode event = objectMapper.readTree(message);

            String operation = event.get("op").asText();

            log.info("Book CDC Operation: {}", operation);

            switch (operation) {

                case "r", "c", "u" ->
                        saveOrUpdateBook(event);

                case "d" ->
                        deleteBook(event);

                default ->
                        log.warn(
                                "Unhandled book CDC operation: {}",
                                operation
                        );
            }

        } catch (Exception e) {

            log.error(
                    "Failed to process book CDC event",
                    e
            );
        }
    }

    private void saveOrUpdateBook(JsonNode event) {

        JsonNode after = event.get("after");

        if (after == null || after.isNull()) {
            return;
        }

        BookDocument book = new BookDocument();

        book.setId(after.get("id").asLong());

        book.setBookTitle(
                after.get("book_title").asText()
        );

        book.setBookAuthorName(
                after.get("book_author_name").asText()
        );

        if (!after.get("posted_date").isNull()) {
            book.setPostedDate(
                    Instant.parse(
                            after.get("posted_date").asText()
                    )
            );
        }

        if (!after.get("content_type").isNull()) {
            book.setContentType(
                    after.get("content_type").asText()
            );
        }

        if (!after.get("book_description").isNull()) {
            book.setBookDescription(
                    after.get("book_description").asText()
            );
        }

        if (!after.get("cover_image_key").isNull()) {
            book.setCoverImageKey(
                    after.get("cover_image_key").asText()
            );
        }

        if (!after.get("pdf_storage_key").isNull()) {
            book.setPdfStorageKey(
                    after.get("pdf_storage_key").asText()
            );
        }

        bookRepository.save(book);

        log.info(
                "Book saved to MongoDB. id={}",
                book.getId()
        );
    }

    private void deleteBook(JsonNode event) {

        JsonNode before = event.get("before");

        if (before == null || before.isNull()) {
            return;
        }

        Long id = before.get("id").asLong();

        bookRepository.deleteById(id);

        log.info(
                "Book deleted from MongoDB. id={}",
                id
        );
    }
}