package com.epam.book_review_svc.controller;

import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookRequestDto;
import com.epam.book_review_svc.model.dto.BookResponseDto;
import com.epam.book_review_svc.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping(value = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BookResponseDto> getBooks() {
        return bookService.getAllBooks().stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping(value = "/books/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponseDto getBook(@PathVariable String id) {
        return toResponse(bookService.getBook(id));
    }

    @PostMapping(value = "/books", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookRequestDto request) {
        BookResponseDto response = toResponse(bookService.createBook(request));
        return ResponseEntity.created(URI.create("/api/v1/books/" + response.getId())).body(response);
    }

    @PutMapping(value = "/books/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BookResponseDto replaceBook(@PathVariable String id, @Valid @RequestBody BookRequestDto request) {
        return toResponse(bookService.replaceBook(id, request));
    }

    @DeleteMapping(value = "/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    private BookResponseDto toResponse(Book book) {
        return BookResponseDto.builder()
            .id(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .author(book.getAuthor())
            .build();
    }
}
