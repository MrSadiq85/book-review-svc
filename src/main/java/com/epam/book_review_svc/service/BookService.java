package com.epam.book_review_svc.service;

import com.epam.book_review_svc.exception.BookNotFoundException;
import com.epam.book_review_svc.exception.DuplicateIsbnException;
import com.epam.book_review_svc.exception.InvalidBookException;
import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookRequestDto;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookService {

    private final JsonFileBookRepository repository;
    private final Validator validator;

    public BookService(JsonFileBookRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public Book getBook(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));
    }

    public Book createBook(BookRequestDto request) {
        validateBookRequest(request);

        String normalizedIsbn = normalizeIsbn(request.getIsbn());
        boolean duplicateIsbn = repository.findAll().stream()
            .anyMatch(book -> normalizeIsbn(book.getIsbn()).equals(normalizedIsbn));

        if (duplicateIsbn) {
            throw new DuplicateIsbnException("isbn already exists for another book");
        }

        Book newBook = Book.builder()
            .id(UUID.randomUUID().toString())
            .isbn(normalizedIsbn)
            .title(request.getTitle().trim())
            .author(request.getAuthor().trim())
            .build();

        return repository.create(newBook);
    }

    public Book replaceBook(String id, BookRequestDto request) {
        validateBookRequest(request);

        String trimmedId = id == null ? "" : id.trim();
        if (trimmedId.isBlank()) {
            throw new BookNotFoundException("Book not found: " + id);
        }

        String normalizedIsbn = normalizeIsbn(request.getIsbn());
        if (!isValidIsbn(normalizedIsbn)) {
            throw new InvalidBookException("isbn is invalid");
        }

        String normalizedTitle = request.getTitle() == null ? "" : request.getTitle().trim();
        String normalizedAuthor = request.getAuthor() == null ? "" : request.getAuthor().trim();

        if (normalizedTitle.isBlank() || normalizedAuthor.isBlank()) {
            throw new InvalidBookException("The book representation is invalid.");
        }

        repository.findById(trimmedId)
            .orElseThrow(() -> new BookNotFoundException("Book not found: " + trimmedId));

        boolean duplicateIsbn = repository.findAll().stream()
            .filter(book -> !trimmedId.equals(book.getId()))
            .anyMatch(book -> normalizeIsbn(book.getIsbn()).equals(normalizedIsbn));

        if (duplicateIsbn) {
            throw new DuplicateIsbnException("isbn already exists for another book");
        }

        Book updatedBook = Book.builder()
            .id(trimmedId)
            .isbn(normalizedIsbn)
            .title(normalizedTitle)
            .author(normalizedAuthor)
            .build();

        return repository.replace(trimmedId, updatedBook);
    }

    private void validateBookRequest(BookRequestDto request) {
        if (request == null) {
            throw new InvalidBookException("The book representation is invalid.");
        }

        Set<ConstraintViolation<BookRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("The book representation is invalid.");
            throw new InvalidBookException(message);
        }

        String normalizedIsbn = normalizeIsbn(request.getIsbn());
        if (!isValidIsbn(normalizedIsbn)) {
            throw new InvalidBookException("isbn is invalid");
        }

        if (request.getTitle().trim().isBlank() || request.getAuthor().trim().isBlank()) {
            throw new InvalidBookException("The book representation is invalid.");
        }
    }

    public void deleteBook(String id) {
        if (id == null || id.isBlank()) {
            throw new BookNotFoundException("Book not found: " + id);
        }

        repository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));

        repository.delete(id);
    }

    private String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return "";
        }
        return isbn.trim().replace("-", "").replace(" ", "").replace("x", "X");
    }

    private boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }

        String normalized = normalizeIsbn(isbn);
        if (normalized.length() == 10) {
            return normalized.matches("[0-9]{9}[0-9X]");
        }
        if (normalized.length() == 13) {
            return normalized.matches("[0-9]{13}");
        }
        return false;
    }
}
