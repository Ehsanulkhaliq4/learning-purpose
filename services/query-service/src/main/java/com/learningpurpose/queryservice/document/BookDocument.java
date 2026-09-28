package com.learningpurpose.queryservice.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "books")
public class BookDocument {

    @Id
    private Long id;

    private String bookTitle;

    private String bookAuthorName;

    private Instant postedDate;

    private String contentType;

    private String bookDescription;

    private String coverImageKey;

    private String pdfStorageKey;
}