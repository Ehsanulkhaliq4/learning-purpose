package com.learningpurpose.queryservice.service;

import com.learningpurpose.queryservice.document.BookDocument;
import com.learningpurpose.queryservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookQueryService {

    private final BookRepository bookRepository;

    public List<BookDocument> getAllBooks() {
        return bookRepository.findAll();
    }

    public BookDocument getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Book not found with id: " + id));
    }
}