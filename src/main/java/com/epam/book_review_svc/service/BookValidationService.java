package com.epam.book_review_svc.service;

import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookValidationService {
    private final JsonFileBookRepository bookRepository;

    public BookValidationService(JsonFileBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public boolean isBookValid(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            return false;
        }

        List<Book> books = bookRepository.findAll();
        return books.stream()
                .anyMatch(b -> b.getId().equals(bookId));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}
