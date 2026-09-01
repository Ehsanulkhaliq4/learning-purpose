package com.learningpurpose.bookstoreservice.service;

import com.learningpurpose.bookstoreservice.dto.BookRequestDto;
import com.learningpurpose.bookstoreservice.dto.BookResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    BookResponseDto createBook(BookRequestDto request, MultipartFile coverImage, MultipartFile pdfFile);
    BookResponseDto updateBook(Long id, BookRequestDto request, MultipartFile coverImage, MultipartFile pdfFile);
    BookResponseDto getBookById(Long id);
    Page<BookResponseDto> getAllBooks(Pageable pageable);
    List<BookResponseDto> searchBooks(String query);
    void deleteBook(Long id);
    ResponseEntity<InputStreamResource> downloadBookPdf(Long id);
}