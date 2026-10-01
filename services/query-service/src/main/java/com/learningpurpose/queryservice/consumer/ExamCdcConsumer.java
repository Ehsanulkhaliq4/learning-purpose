package com.learningpurpose.queryservice.consumer;

import com.learningpurpose.queryservice.document.ExamCategoryDocument;
import com.learningpurpose.queryservice.document.QuizDocument;
import com.learningpurpose.queryservice.repository.ExamCategoryRepository;
import com.learningpurpose.queryservice.repository.QuizRepository;
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
public class ExamCdcConsumer {

    private final ObjectMapper objectMapper;
    private final ExamCategoryRepository categoryRepository;
    private final QuizRepository quizRepository;


    // =========================================================
    // CATEGORY
    // =========================================================

    @KafkaListener(
            topics = "exam.public.categories",
            groupId = "query-service"
    )
    public void consumeCategory(String message) {

        if (message == null) {
            log.warn("Received null category CDC message");
            return;
        }

        try {

            JsonNode event = objectMapper.readTree(message);

            String operation = event.get("op").asString();

            log.info("Exam Category CDC Operation: {}", operation);

            switch (operation) {

                case "r", "c", "u" ->
                        saveOrUpdateCategory(event);

                case "d" ->
                        deleteCategory(event);

                default ->
                        log.warn(
                                "Unhandled category CDC operation: {}",
                                operation
                        );
            }

        } catch (Exception e) {

            log.error(
                    "Failed to process category CDC event",
                    e
            );
        }
    }


    private void saveOrUpdateCategory(JsonNode event) {

        JsonNode after = event.get("after");

        if (after == null || after.isNull()) {
            return;
        }

        ExamCategoryDocument category =
                new ExamCategoryDocument();

        category.setId(
                after.get("id").asLong()
        );

        category.setTitle(
                after.get("title").asString()
        );

        if (!after.get("description").isNull()) {

            category.setDescription(
                    after.get("description").asString()
            );
        }

        if (!after.get("created_at").isNull()) {

            category.setCreatedAt(
                    Instant.parse(
                            after.get("created_at").asString()
                    )
            );
        }

        categoryRepository.save(category);

        log.info(
                "Exam category saved to MongoDB. id={}",
                category.getId()
        );
    }


    private void deleteCategory(JsonNode event) {

        JsonNode before = event.get("before");

        if (before == null || before.isNull()) {
            return;
        }

        Long id = before.get("id").asLong();

        categoryRepository.deleteById(id);

        log.info(
                "Exam category deleted from MongoDB. id={}",
                id
        );
    }


    // =========================================================
    // QUIZ
    // =========================================================

    @KafkaListener(
            topics = "exam.public.quizzes",
            groupId = "query-service"
    )
    public void consumeQuiz(String message) {

        if (message == null) {
            log.warn("Received null quiz CDC message");
            return;
        }

        try {

            JsonNode event = objectMapper.readTree(message);

            String operation = event.get("op").asString();

            log.info("Quiz CDC Operation: {}", operation);

            switch (operation) {

                case "r", "c", "u" ->
                        saveOrUpdateQuiz(event);

                case "d" ->
                        deleteQuiz(event);

                default ->
                        log.warn(
                                "Unhandled quiz CDC operation: {}",
                                operation
                        );
            }

        } catch (Exception e) {

            log.error(
                    "Failed to process quiz CDC event",
                    e
            );
        }
    }


    private void saveOrUpdateQuiz(JsonNode event) {

        JsonNode after = event.get("after");

        if (after == null || after.isNull()) {
            return;
        }

        QuizDocument quiz =
                new QuizDocument();

        quiz.setId(
                after.get("id").asLong()
        );

        quiz.setCategoryId(
                after.get("category_id").asLong()
        );

        quiz.setTitle(
                after.get("title").asString()
        );

        if (!after.get("description").isNull()) {

            quiz.setDescription(
                    after.get("description").asString()
            );
        }

        quiz.setMaxMarks(
                after.get("max_marks").asInt()
        );

        quiz.setNumberOfQuestions(
                after.get("number_of_questions").asInt()
        );

        quiz.setActive(
                after.get("active").asBoolean()
        );

        if (!after.get("created_at").isNull()) {

            quiz.setCreatedAt(
                    Instant.parse(
                            after.get("created_at").asString()
                    )
            );
        }

        quizRepository.save(quiz);

        log.info(
                "Quiz saved to MongoDB. id={}",
                quiz.getId()
        );
    }


    private void deleteQuiz(JsonNode event) {

        JsonNode before = event.get("before");

        if (before == null || before.isNull()) {
            return;
        }

        Long id = before.get("id").asLong();

        quizRepository.deleteById(id);

        log.info(
                "Quiz deleted from MongoDB. id={}",
                id
        );
    }
}