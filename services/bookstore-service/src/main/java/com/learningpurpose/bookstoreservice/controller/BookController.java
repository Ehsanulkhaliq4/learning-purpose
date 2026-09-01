package com.learningpurpose.bookstoreservice.controller;

import com.learningpurpose.bookstoreservice.dto.BookRequestDto;
import com.learningpurpose.bookstoreservice.dto.BookResponseDto;
import com.learningpurpose.bookstoreservice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponseDto> createBook(
            @Valid @RequestPart("book") BookRequestDto request,
            @RequestPart(value = "coverImage",required = false) MultipartFile coverImage,
            @RequestPart(value = "pdfFile", required = false) MultipartFile pdfFile
            ){
        return new ResponseEntity<>(bookService.createBook(request, coverImage, pdfFile), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable Long id,
            @Valid @RequestPart("book") BookRequestDto request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "pdfFile", required = false) MultipartFile pdfFile
    ) {
        return ResponseEntity.ok(bookService.updateBook(id, request, coverImage, pdfFile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDto>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookService.getAllBooks(PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponseDto>> searchBooks(@RequestParam String query) {
        return ResponseEntity.ok(bookService.searchBooks(query));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Long id) {
        return bookService.downloadBookPdf(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
