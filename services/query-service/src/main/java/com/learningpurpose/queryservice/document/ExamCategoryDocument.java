package com.learningpurpose.queryservice.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "exam_categories")
public class ExamCategoryDocument {

    @Id
    private Long id;

    private String title;

    private String description;

    private Instant createdAt;
}