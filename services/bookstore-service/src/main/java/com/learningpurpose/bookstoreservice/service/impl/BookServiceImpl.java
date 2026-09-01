package com.learningpurpose.bookstoreservice.service.impl;

import com.learningpurpose.bookstoreservice.dto.BookRequestDto;
import com.learningpurpose.bookstoreservice.dto.BookResponseDto;
import com.learningpurpose.bookstoreservice.exception.BookNotFoundException;
import com.learningpurpose.bookstoreservice.model.Books;
import com.learningpurpose.bookstoreservice.repository.BookRepository;
import com.learningpurpose.bookstoreservice.service.BookService;
import com.learningpurpose.bookstoreservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public BookResponseDto createBook(BookRequestDto request, MultipartFile coverImage, MultipartFile pdfFile) {
        String coverKey = storageService.uploadFile(coverImage, "covers");
        String pdfKey = storageService.uploadFile(pdfFile, "documents");

        Books book = Books.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .description(request.getDescription())
                .contentType(request.getContentType() != null ? request.getContentType() : "PDF")
                .coverImageKey(coverKey)
                .pdfStorageKey(pdfKey)
                .postedDate(Instant.now())
                .build();

        Books saved = bookRepository.save(book);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public BookResponseDto updateBook(Long id, BookRequestDto request, MultipartFile coverImage, MultipartFile pdfFile) {
        Books book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        if (request.getContentType() != null) {
            book.setContentType(request.getContentType());
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            storageService.deleteFile(book.getCoverImageKey());
            book.setCoverImageKey(storageService.uploadFile(coverImage, "covers"));
        }

        if (pdfFile != null && !pdfFile.isEmpty()) {
            storageService.deleteFile(book.getPdfStorageKey());
            book.setPdfStorageKey(storageService.uploadFile(pdfFile, "documents"));
        }

        return mapToDto(bookRepository.save(book));
    }

    @Override
    public BookResponseDto getBookById(Long id) {
        Books book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return mapToDto(book);
    }

    @Override
    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        return bookRepository.findAllByOrderByPostedDateDesc(pageable).map(this::mapToDto);
    }

    @Override
    public List<BookResponseDto> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Books book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        storageService.deleteFile(book.getCoverImageKey());
        storageService.deleteFile(book.getPdfStorageKey());
        bookRepository.delete(book);
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadBookPdf(Long id) {
        Books book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        if (book.getPdfStorageKey() == null || book.getPdfStorageKey().isBlank()) {
            throw new BookNotFoundException("No PDF file attached to book id: " + id);
        }

        InputStream inputStream = storageService.downloadFile(book.getPdfStorageKey());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + book.getTitle() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(inputStream));
    }

    private BookResponseDto mapToDto(Books book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .contentType(book.getContentType())
                .postedDate(book.getPostedDate())
                .coverImageUrl(storageService.generatePresignedUrl(book.getCoverImageKey()))
                .pdfDownloadUrl(storageService.generatePresignedUrl(book.getPdfStorageKey()))
                .build();
    }
}
