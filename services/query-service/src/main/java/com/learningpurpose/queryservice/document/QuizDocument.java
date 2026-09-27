package com.learningpurpose.queryservice.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "quizzes")
public class QuizDocument {

    @Id
    private Long id;

    private Long categoryId;

    private String title;

    private String description;

    private Integer maxMarks;

    private Integer numberOfQuestions;

    private Boolean active;

    private Instant createdAt;
}