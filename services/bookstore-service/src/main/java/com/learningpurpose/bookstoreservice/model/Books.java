package com.learningpurpose.bookstoreservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "book_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Books {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_title", nullable = false)
    private String title;

    @Column(name = "book_author_name", nullable = false, length = 150)
    private String author;

    @Column(name = "posted_date")
    private Instant postedDate;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "book_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_key", length = 500)
    private String coverImageKey;

    @Column(name = "pdf_storage_key", length = 500)
    private String pdfStorageKey;

    @PrePersist
    protected void onCreate() {
        if (this.postedDate == null) {
            this.postedDate = Instant.now();
        }
    }
}