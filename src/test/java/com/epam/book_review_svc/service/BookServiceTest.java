package com.epam.book_review_svc.service;

import com.epam.book_review_svc.exception.DuplicateIsbnException;
import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookRequestDto;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookServiceTest {

    private static Path storePath;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        storePath = Path.of(System.getProperty("java.io.tmpdir"), "book-service-test-" + UUID.randomUUID() + ".json");
        registry.add("book.storage.path", () -> storePath.toString());
    }

    @Autowired
    private BookService bookService;

    @Autowired
    private JsonFileBookRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        Files.deleteIfExists(storePath);
        Files.writeString(storePath, "[]");
    }

    @Test
    void replaceBookRejectsDuplicateIsbnAcrossBooks() {
        repository.replace("book-1", Book.builder()
            .id("book-1")
            .isbn("9780134685991")
            .title("Effective Java")
            .author("Joshua Bloch")
            .build());

        repository.replace("book-2", Book.builder()
            .id("book-2")
            .isbn("9780134685992")
            .title("Head First Java")
            .author("Kathy Sierra")
            .build());

        BookRequestDto duplicateRequest = BookRequestDto.builder()
            .isbn("9780134685992")
            .title("Java in Action")
            .author("Bruce Eckel")
            .build();

        assertThatThrownBy(() -> bookService.replaceBook("book-1", duplicateRequest))
            .isInstanceOf(DuplicateIsbnException.class);
    }

    @Test
    void getBookReturnsStoredBook() {
        repository.replace("book-1", Book.builder()
            .id("book-1")
            .isbn("9780134685991")
            .title("Effective Java")
            .author("Joshua Bloch")
            .build());

        Book book = bookService.getBook("book-1");
        assertThat(book.getTitle()).isEqualTo("Effective Java");
    }
}
