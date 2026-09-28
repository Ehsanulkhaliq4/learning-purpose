package com.learningpurpose.queryservice.controller;

import com.learningpurpose.queryservice.document.BookDocument;
import com.learningpurpose.queryservice.service.BookQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/books")
@RequiredArgsConstructor
public class BookQueryController {

    private final BookQueryService bookQueryService;

    @GetMapping
    public ResponseEntity<List<BookDocument>> getAllBooks() {
        return ResponseEntity.ok(
                bookQueryService.getAllBooks()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDocument> getBookById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                bookQueryService.getBookById(id)
        );
    }
}