package com.learningpurpose.bookstoreservice.repository;

import com.learningpurpose.bookstoreservice.model.Books;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Books, Long> {
    List<Books> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    Page<Books> findAllByOrderByPostedDateDesc(Pageable pageable);
}